use clap::{Parser, Subcommand};
use seyfr_core::{Core, ProgressSink};

/// Console progress reporter for CLI usage
struct ConsoleProgress;

impl ProgressSink for ConsoleProgress {
    fn on_file_start(&self, name: String, current: u64, total: u64) {
        println!("[{:>3}/{:<3}] Starting: {}", current, total, name);
    }

    fn on_file_progress(&self, name: String, bytes: u64, total: u64) {
        if total > 0 {
            let pct = (bytes as f64 / total as f64) * 100.0;
            println!("  {}: {:.1}% ({}/{} bytes)", name, pct, bytes, total);
        } else {
            println!("  {}: {} bytes", name, bytes);
        }
    }

    fn on_file_complete(&self, name: String, current: u64, total: u64) {
        println!("[{:>3}/{:<3}] Complete: {}", current, total, name);
    }

    fn on_complete(&self, message: String) {
        println!("✅ {}", message);
    }

    fn on_error(&self, message: String) {
        eprintln!("❌ {}", message);
    }
}

#[derive(Parser)]
#[command(name = "seyfr")]
#[command(about = "Seyfr CLI — peer-to-peer file transfer")]
struct Cli {
    #[command(subcommand)]
    command: Commands,
}

#[derive(Subcommand)]
enum Commands {
    /// Send a file or folder (returns a ticket)
    Send {
        /// Path to the file or folder to send
        path: String,
        /// Data directory for the node
        #[arg(short, long, default_value = ".seyfr")]
        data_dir: String,
        /// Keep the node running after generating the ticket (required for receiver to download)
        #[arg(short, long)]
        listen: bool,
    },
    /// Receive files from a ticket
    Receive {
        /// Ticket string from the sender
        ticket: String,
        /// Destination directory
        #[arg(short, long, default_value = ".")]
        dest: String,
        /// Data directory for the node
        #[arg(long, default_value = ".seyfr")]
        data_dir: String,
    },
    /// Show the node ID (for debugging)
    NodeId {
        /// Data directory for the node
        #[arg(short, long, default_value = ".seyfr")]
        data_dir: String,
    },
}

fn main() {
    let cli = Cli::parse();

    match cli.command {
        Commands::Send { path, data_dir, listen } => {
            let core = Core::new(data_dir).expect("Failed to initialize Seyfr core");
            println!("Node ID: {}", core.node_id());
            println!("Sending: {}", path);
            println!("Generating ticket...\n");

            let progress = ConsoleProgress;
            match core.send(path, Some(Box::new(progress))) {
                Ok(ticket) => {
                    println!("\n🎫 Ticket (share this with recipient):");
                    println!("{}", ticket);

                    if listen {
                        println!("\n🔊 Listening for connections... Press Ctrl+C to stop.");
                        let (tx, rx) = std::sync::mpsc::channel();
                        ctrlc::set_handler(move || {
                            tx.send(()).ok();
                        })
                        .expect("Error setting Ctrl-C handler");
                        rx.recv().expect("Error waiting for signal");
                        println!("\n👋 Shutting down.");
                    }
                }
                Err(e) => {
                    eprintln!("Send failed: {}", e);
                    std::process::exit(1);
                }
            }
        }
        Commands::Receive { ticket, dest, data_dir } => {
            let core = Core::new(data_dir).expect("Failed to initialize Seyfr core");
            println!("Node ID: {}", core.node_id());
            println!("Receiving to: {}\n", dest);

            let progress = ConsoleProgress;
            match core.receive(ticket, dest, Some(Box::new(progress))) {
                Ok(_) => {
                    println!("\nTransfer complete.");
                }
                Err(e) => {
                    eprintln!("Receive failed: {}", e);
                    std::process::exit(1);
                }
            }
        }
        Commands::NodeId { data_dir } => {
            let core = Core::new(data_dir).expect("Failed to initialize Seyfr core");
            println!("{}", core.node_id());
        }
    }
}
