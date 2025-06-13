using System.Diagnostics;
using System.Net;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Text;
using Newtonsoft.Json;

namespace local;

class Program
{
    internal static async Task Main()
    {
        using (TcpListener socket = new(IPAddress.Any, 5080))
        {
            socket.Start();

            while (true)
            {
                string type;
                string prompt;
                string model;
                string format;
                
                TcpClient client = socket.AcceptTcpClient();
                NetworkStream stream = client.GetStream();
                BinaryReader reader = new(stream);
                
                byte[] lengthBuffer = new byte[4];
                reader.Read(lengthBuffer, 0, 4);
                if (BitConverter.IsLittleEndian) lengthBuffer = lengthBuffer.Reverse().ToArray();
                int length = BitConverter.ToInt32(lengthBuffer, 0);
                
                byte[] buffer = new byte[length];
                for (int bytesRead = 0;
                     bytesRead < length;
                     bytesRead += stream.Read(buffer, bytesRead, length - bytesRead));
                type = Encoding.UTF8.GetString(buffer, 0, buffer.Length);

                if (type == "GRAB_MODELS")
                {
                    byte[] models = Encoding.UTF8.GetBytes(get_models());
                    stream.Write(BitConverter.GetBytes(models.Length), 0, 4);
                    stream.Write(models, 0, models.Length);
                    stream.Flush();
                    
                    stream.Close();
                    client.Close();
                    continue;
                }
                
                lengthBuffer = new byte[4];
                reader.Read(lengthBuffer, 0, 4);
                if (BitConverter.IsLittleEndian) lengthBuffer = lengthBuffer.Reverse().ToArray();
                length = BitConverter.ToInt32(lengthBuffer, 0);
                
                buffer = new byte[length];
                for (int bytesRead = 0;
                     bytesRead < length;
                     bytesRead += stream.Read(buffer, bytesRead, length - bytesRead));
                prompt = Encoding.UTF8.GetString(buffer, 0, buffer.Length);
                
                lengthBuffer = new byte[4];
                reader.Read(lengthBuffer, 0, 4);
                if (BitConverter.IsLittleEndian) lengthBuffer = lengthBuffer.Reverse().ToArray();
                length = BitConverter.ToInt32(lengthBuffer, 0);
                
                buffer = new byte[length];
                for (int bytesRead = 0;
                     bytesRead < length;
                     bytesRead += stream.Read(buffer, bytesRead, length - bytesRead));
                model = Encoding.UTF8.GetString(buffer, 0, buffer.Length);
                
                lengthBuffer = new byte[4];
                reader.Read(lengthBuffer, 0, 4);
                if (BitConverter.IsLittleEndian) lengthBuffer = lengthBuffer.Reverse().ToArray();
                length = BitConverter.ToInt32(lengthBuffer, 0);
                
                buffer = new byte[length];
                for (int bytesRead = 0;
                     bytesRead < length;
                     bytesRead += stream.Read(buffer, bytesRead, length - bytesRead));
                format = Encoding.UTF8.GetString(buffer, 0, buffer.Length);
                
                Console.WriteLine($"Model: {model}\tPrompt: {prompt}");
                string text = await llm(model, prompt, format);
                Console.WriteLine(text);
                byte[] message = Encoding.UTF8.GetBytes(text);
                
                stream.Write(BitConverter.GetBytes(message.Length).Reverse().ToArray(), 0, 4);
                stream.Write(message, 0, message.Length);
                stream.Flush();
                
                stream.Close();
                client.Close();
            }
        }
    }

    private static async Task<String> llm(string _model, string _prompt, string _format)
    {
        const string url = "http://localhost:11434/";

        if (_prompt == "") return "";

        using HttpClient ollamaClient = new();
        ollamaClient.BaseAddress = new Uri(url);
        
        var ollamaPayload = new
        {
            model = _model,
            prompt = _prompt,
            stream = false
        };

        string ollamaText;
        try
        {
            StringContent ollamaContent = new(JsonConvert.SerializeObject(ollamaPayload), Encoding.UTF8, "application/json");
            HttpResponseMessage ollamaResponse = await ollamaClient.PostAsync("/api/generate", ollamaContent);
            if (!ollamaResponse.IsSuccessStatusCode)
            {
                Console.WriteLine($"Error: {ollamaResponse.ReasonPhrase}");
                return "";
            }

            ollamaText = await ollamaResponse.Content.ReadAsStringAsync();
        }
        catch (Exception ex)
        {
            Console.WriteLine(ex.Message);
            return "";
        }

        if (_format != "json")
        {
            dynamic ollamaJson = JsonConvert.DeserializeObject(ollamaText);
            ollamaText = (string) ollamaJson["response"];
        }
        return ollamaText;
    }

    private static string get_models()
    {
        string models = "";
        string[] modelsArray;
        
        try
        {
            ProcessStartInfo startInfo = new ProcessStartInfo()
            {
                FileName = "ollama",
                Arguments = "list",
                UseShellExecute = false,
                RedirectStandardOutput = true,
                CreateNoWindow = true
            };

            using (Process process = Process.Start(startInfo))
            {
                modelsArray = process.StandardOutput.ReadToEnd().Split("\n");
                process.WaitForExit();

            }
        }
        catch (Exception e)
        {
            return models;
        }

        for (int i = 1 ; i < modelsArray.Length; i++)
            models += modelsArray[i].Split(":")[0] + "\n";
        Console.WriteLine($"Models: {models}");
        return modelsArray.Length > 1 ? models.Substring(0, models.Length - 1) : "";
    }
}