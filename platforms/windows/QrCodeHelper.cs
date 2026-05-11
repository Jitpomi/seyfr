using Microsoft.UI.Xaml.Media.Imaging;
using QRCoder;
using System.IO;

namespace Seyfr
{
    /// <summary>
    /// Generates a WinUI 3 WriteableBitmap QR code from a text string using QRCoder.
    /// </summary>
    public static class QrCodeHelper
    {
        public static WriteableBitmap Generate(string text, int pixelsPerModule = 8)
        {
            var qrGenerator = new QRCodeGenerator();
            var qrCodeData = qrGenerator.CreateQrCode(text, QRCodeGenerator.ECCLevel.Q);
            int moduleCount = qrCodeData.ModuleMatrix.Count;
            int size = moduleCount * pixelsPerModule;

            var writeableBitmap = new WriteableBitmap(size, size);
            byte[] pixels = new byte[size * size * 4]; // BGRA8

            for (int y = 0; y < size; y++)
            {
                for (int x = 0; x < size; x++)
                {
                    int moduleX = x / pixelsPerModule;
                    int moduleY = y / pixelsPerModule;
                    bool isBlack = qrCodeData.ModuleMatrix[moduleY][moduleX];

                    int index = (y * size + x) * 4;
                    byte color = isBlack ? (byte)0 : (byte)255;
                    pixels[index] = color;     // B
                    pixels[index + 1] = color; // G
                    pixels[index + 2] = color; // R
                    pixels[index + 3] = 255;   // A
                }
            }

            using (Stream stream = writeableBitmap.PixelBuffer.AsStream())
            {
                stream.Write(pixels, 0, pixels.Length);
            }

            return writeableBitmap;
        }
    }
}
