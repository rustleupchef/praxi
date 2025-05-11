using System.Net;
using System.Net.Sockets;

namespace local;

class Program
{
    internal static void Main()
    {
        using (TcpListener socket = new(IPAddress.Any, 8080))
        {
            socket.Start();

            while (true)
            {
                TcpClient client = socket.AcceptTcpClient();
                NetworkStream stream = client.GetStream();
                BinaryReader reader = new(stream);
            }
        }
    }
}