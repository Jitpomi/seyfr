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

            try
            {
                var hWnd = WinRT.Interop.WindowNative.GetWindowHandle(this);
                var windowId = Microsoft.UI.Win32Interop.GetWindowIdFromWindow(hWnd);
                var appWindow = Microsoft.UI.Windowing.AppWindow.GetFromWindowId(windowId);
                
                // Increase default window size so the Support view fits without scrolling
                appWindow.Resize(new Windows.Graphics.SizeInt32 { Width = 1100, Height = 880 });

                var iconPath = FindIconPath();
                if (iconPath != null)
                {
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
                // Override the default Keyboard focus state to Pointer so the dark focus ring doesn't appear on startup
                NavView.Focus(FocusState.Pointer);

                StartWaveAnimation();
            };
        }

        private DispatcherTimer? _infoBarTimer;
        private DispatcherTimer? _waveTimer;
        private DateTime _waveStartTime;
        private Microsoft.UI.Xaml.Shapes.Ellipse[] _waveRings = new Microsoft.UI.Xaml.Shapes.Ellipse[8];

        private void StartWaveAnimation()
        {
            RingsCanvas.Children.Clear();
            var brush = new Microsoft.UI.Xaml.Media.SolidColorBrush(Microsoft.UI.ColorHelper.FromArgb(255, 128, 128, 128));

            for (int i = 0; i < 8; i++)
            {
                var el = new Microsoft.UI.Xaml.Shapes.Ellipse
                {
                    Stroke = brush,
                    StrokeThickness = 0.5
                };
                _waveRings[i] = el;
                RingsCanvas.Children.Add(el);
            }

            _waveStartTime = DateTime.Now;
            _waveTimer = new DispatcherTimer { Interval = TimeSpan.FromMilliseconds(16) };
            _waveTimer.Tick += (s, e) =>
            {
                var elapsed = (DateTime.Now - _waveStartTime).TotalMilliseconds;
                var phase = (elapsed % 1200.0) / 1200.0;

                double centerX = 140.0;
                double centerY = 140.0;
                double baseRadius = 40.0;
                double spacing = 14.0;
                
                bool isAnimating = ViewModel.IsBusy;
                double maxAlpha = isAnimating ? 0.7 : 0.4;

                for (int i = 0; i < 8; i++)
                {
                    double currentPhase = i + phase;
                    double radius = baseRadius + (currentPhase * spacing);
                    double alpha = Math.Max(0.0, maxAlpha - (currentPhase / 12.0));

                    _waveRings[i].Width = radius * 2;
                    _waveRings[i].Height = radius * 2;
                    Canvas.SetLeft(_waveRings[i], centerX - radius);
                    Canvas.SetTop(_waveRings[i], centerY - radius);
                    _waveRings[i].Opacity = alpha;
                }
            };
            _waveTimer.Start();
        }

        private void ViewModel_PropertyChanged(object? sender, System.ComponentModel.PropertyChangedEventArgs e)
        {
            if (e.PropertyName == nameof(AppViewModel.Status) && !string.IsNullOrEmpty(ViewModel.Status))
            {
                DispatcherQueue.TryEnqueue(() =>
                {
                    StatusInfoBar.Message = ViewModel.Status;
                    StatusInfoBar.Severity = ViewModel.IsError ? Microsoft.UI.Xaml.Controls.InfoBarSeverity.Error : Microsoft.UI.Xaml.Controls.InfoBarSeverity.Success;
                    StatusInfoBar.IsOpen = true;

                    if (_infoBarTimer != null)
                    {
                        _infoBarTimer.Stop();
                    }

                    _infoBarTimer = new DispatcherTimer();
                    _infoBarTimer.Interval = TimeSpan.FromSeconds(3);
                    _infoBarTimer.Tick += (s, args) =>
                    {
                        StatusInfoBar.IsOpen = false;
                        _infoBarTimer.Stop();
                    };
                    _infoBarTimer.Start();
                });
            }
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