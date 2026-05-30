using Microsoft.UI.Xaml;
using System;
using uniffi.seyfr_core;
using Windows.ApplicationModel.DataTransfer;
using Windows.Storage;
using Microsoft.UI.Xaml.Input;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Media;
using System.Threading.Tasks;
using Microsoft.UI;

// To learn more about WinUI, the WinUI project structure,
// and more about our project templates, see: http://aka.ms/winui-project-info.

namespace Seyfr
{
    /// <summary>
    /// Main window that hosts the application UI with ViewModel-based data binding.
    /// </summary>
    public sealed partial class MainWindow : Window
    {
        public AppViewModel ViewModel { get; }

        public MainWindow()
        {
            this.InitializeComponent();

            ExtendsContentIntoTitleBar = true;
            SetTitleBar(AppTitleBar);

            // Set window icon
            try
            {
                var iconPath = FindIconPath();
                if (iconPath != null)
                {
                    var hWnd = WinRT.Interop.WindowNative.GetWindowHandle(this);
                    var windowId = Microsoft.UI.Win32Interop.GetWindowIdFromWindow(hWnd);
                    var appWindow = Microsoft.UI.Windowing.AppWindow.GetFromWindowId(windowId);
                    appWindow.SetIcon(iconPath);
                }
                else
                {
                    System.Diagnostics.Debug.WriteLine("Failed to find app.ico in any candidate path.");
                }
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Failed to set window icon: {ex.Message}");
            }

            ViewModel = new AppViewModel();
            RootGrid.DataContext = ViewModel;

            ViewModel.PropertyChanged += ViewModel_PropertyChanged;

            RootGrid.Loaded += (s, e) =>
            {
                if (RootGrid.Resources["BreathingAnimation"] is Microsoft.UI.Xaml.Media.Animation.Storyboard breathingAnim)
                {
                    breathingAnim.Begin();
                }
            };
        }

        private void ViewModel_PropertyChanged(object? sender, System.ComponentModel.PropertyChangedEventArgs e)
        {
            if (e.PropertyName == nameof(AppViewModel.Status) && !string.IsNullOrEmpty(ViewModel.Status))
            {
                ShowSnackbar(ViewModel.Status, ViewModel.IsError);
            }
        }

        private void ShowSnackbar(string message, bool isError)
        {
            DispatcherQueue.TryEnqueue(() =>
            {
                SnackbarText.Text = message;
                SnackbarIcon.Glyph = isError ? "\uE783" : "\uE73E"; // Warning vs Check mark
                SnackbarIcon.Foreground = isError ? new SolidColorBrush(Colors.Red) : new SolidColorBrush(Colors.White);

                var storyboard = new Microsoft.UI.Xaml.Media.Animation.Storyboard();
                var translateAnimation = new Microsoft.UI.Xaml.Media.Animation.DoubleAnimation()
                {
                    To = -20,
                    Duration = TimeSpan.FromMilliseconds(300),
                    EasingFunction = new Microsoft.UI.Xaml.Media.Animation.CubicEase() { EasingMode = Microsoft.UI.Xaml.Media.Animation.EasingMode.EaseOut }
                };
                Microsoft.UI.Xaml.Media.Animation.Storyboard.SetTarget(translateAnimation, SnackbarTransform);
                Microsoft.UI.Xaml.Media.Animation.Storyboard.SetTargetProperty(translateAnimation, "Y");

                var opacityAnimation = new Microsoft.UI.Xaml.Media.Animation.DoubleAnimation()
                {
                    To = 1.0,
                    Duration = TimeSpan.FromMilliseconds(300)
                };
                Microsoft.UI.Xaml.Media.Animation.Storyboard.SetTarget(opacityAnimation, SnackbarBorder);
                Microsoft.UI.Xaml.Media.Animation.Storyboard.SetTargetProperty(opacityAnimation, "Opacity");

                storyboard.Children.Add(translateAnimation);
                storyboard.Children.Add(opacityAnimation);
                storyboard.Begin();

                _ = HideSnackbarAsync();
            });
        }

        private async Task HideSnackbarAsync()
        {
            await Task.Delay(3000);
            DispatcherQueue.TryEnqueue(() =>
            {
                var storyboard = new Microsoft.UI.Xaml.Media.Animation.Storyboard();
                var translateAnimation = new Microsoft.UI.Xaml.Media.Animation.DoubleAnimation()
                {
                    To = 0,
                    Duration = TimeSpan.FromMilliseconds(300),
                    EasingFunction = new Microsoft.UI.Xaml.Media.Animation.CubicEase() { EasingMode = Microsoft.UI.Xaml.Media.Animation.EasingMode.EaseIn }
                };
                Microsoft.UI.Xaml.Media.Animation.Storyboard.SetTarget(translateAnimation, SnackbarTransform);
                Microsoft.UI.Xaml.Media.Animation.Storyboard.SetTargetProperty(translateAnimation, "Y");

                var opacityAnimation = new Microsoft.UI.Xaml.Media.Animation.DoubleAnimation()
                {
                    To = 0.0,
                    Duration = TimeSpan.FromMilliseconds(300)
                };
                Microsoft.UI.Xaml.Media.Animation.Storyboard.SetTarget(opacityAnimation, SnackbarBorder);
                Microsoft.UI.Xaml.Media.Animation.Storyboard.SetTargetProperty(opacityAnimation, "Opacity");

                storyboard.Children.Add(translateAnimation);
                storyboard.Children.Add(opacityAnimation);
                storyboard.Begin();
            });
        }

        private void NavView_SelectionChanged(NavigationView sender, NavigationViewSelectionChangedEventArgs args)
        {
            if (args.SelectedItem is NavigationViewItem item)
            {
                switch (item.Tag?.ToString())
                {
                    case "Send":
                        ViewModel.SelectedTab = TransferTab.Send;
                        break;
                    case "Receive":
                        ViewModel.SelectedTab = TransferTab.Receive;
                        break;
                    case "Support":
                        ViewModel.SelectedTab = TransferTab.Support;
                        break;
                }
            }
        }

        private string? FindIconPath()
        {
            var candidates = new[]
            {
                System.IO.Path.Combine(System.AppContext.BaseDirectory, "Assets", "app.ico"),
                System.IO.Path.Combine(System.AppContext.BaseDirectory, "AppX", "Assets", "app.ico"),
                System.IO.Path.Combine(System.IO.Directory.GetCurrentDirectory(), "Assets", "app.ico"),
                System.IO.Path.Combine(System.IO.Directory.GetCurrentDirectory(), "platforms", "windows", "Assets", "app.ico")
            };

            foreach (var path in candidates)
            {
                if (System.IO.File.Exists(path))
                {
                    return path;
                }
            }
            return null;
        }

        private void BrowseButton_Click(object sender, RoutedEventArgs e)
        {
            ViewModel.SelectSendFileCommand.Execute(null);
        }

        private void DropArea_Tapped(object sender, TappedRoutedEventArgs e)
        {
            ViewModel.SelectSendFileCommand.Execute(null);
        }

        private void DropArea_DragEnter(object sender, DragEventArgs e)
        {
            DragOverlay.Opacity = 1;
        }

        private void DropArea_DragLeave(object sender, DragEventArgs e)
        {
            DragOverlay.Opacity = 0;
        }

        private void DropArea_DragOver(object sender, DragEventArgs e)
        {
            e.AcceptedOperation = DataPackageOperation.Copy;
            e.DragUIOverride.IsCaptionVisible = true;
            e.DragUIOverride.Caption = "Drop to send";
            e.Handled = true;
        }

        private void TicketInput_TextChanged(object sender, TextChangedEventArgs e)
        {
            if (sender is TextBox textBox)
            {
                ViewModel.TicketInput = textBox.Text;
            }
        }

        private async void DropArea_Drop(object sender, DragEventArgs e)
        {
            DragOverlay.Opacity = 0;
            if (e.DataView.Contains(StandardDataFormats.StorageItems))
            {
                var items = await e.DataView.GetStorageItemsAsync();
                if (items.Count > 0)
                {
                    var item = items[0];
                    bool isFolder = item is StorageFolder;
                    ViewModel.SetSendFile(item.Path, item.Name, isFolder);
                }
            }
        }
    }
}