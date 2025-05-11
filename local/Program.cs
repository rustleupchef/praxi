using System.Net;
using System.Net.Sockets;
using System.Text;

namespace local;

class Program
{
    internal static void Main()
    {
        using (TcpListener socket = new(IPAddress.Any, 5080))
        {
            socket.Start();

            while (true)
            {
                string type;
                string prompt;
                string model;
                
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
                    byte[] models = Encoding.UTF8.GetBytes("gemini\nllava\nmistral");
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
                
                Console.WriteLine($"Model: {model}\tPrompt: {prompt}");
                
                byte[] message = Encoding.UTF8.GetBytes("hello world");
                
                stream.Write(BitConverter.GetBytes(message.Length), 0, 4);
                stream.Write(message, 0, message.Length);
                stream.Flush();
                
                stream.Close();
                client.Close();
            }
        }
    }
}