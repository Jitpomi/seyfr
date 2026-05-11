using QRCoder;

namespace Seyfr
{
    /// <summary>
    /// Generates QR code bytes using QRCoder's PngByteQRCode renderer.
    /// This avoids manual pixel manipulation and is more robust for WinUI 3.
    /// </summary>
    public static class QrCodeHelper
    {
        public static byte[] GeneratePngBytes(string text)
        {
            using var qrGenerator = new QRCodeGenerator();
            var qrCodeData = qrGenerator.CreateQrCode(text, QRCodeGenerator.ECCLevel.M);
            using var qrCode = new PngByteQRCode(qrCodeData);
            return qrCode.GetGraphic(10); // 10 pixels per module
        }
    }
}
