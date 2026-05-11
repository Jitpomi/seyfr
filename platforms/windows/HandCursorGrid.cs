using Microsoft.UI.Input;
using Microsoft.UI.Xaml.Controls;

namespace Seyfr
{
    /// <summary>
    /// A Grid that shows a hand cursor when the pointer hovers over it.
    /// </summary>
    public class HandCursorGrid : Grid
    {
        public HandCursorGrid()
        {
            this.ProtectedCursor = InputSystemCursor.Create(InputSystemCursorShape.Hand);
        }
    }
}
