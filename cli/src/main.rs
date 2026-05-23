use clap::{Parser, Subcommand};
use seyfr_core::Core;



#[derive(Parser)]
#[command(name = "seyfr")]
#[command(about = "Seyfr — peer-to-peer file transfer")]
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
        /// Keep the node running after generating the ticket
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
    /// Show the node ID
    NodeId {
        /// Data directory for the node
        #[arg(short, long, default_value = ".seyfr")]
        data_dir: String,
    },
}

fn main() {
    let cli = Cli::parse();

    match cli.command {
        Commands::NodeId { data_dir } => {
            let core = Core::new(data_dir).expect("Failed to initialize Seyfr core");
            println!("{}", core.node_id());
        }
        Commands::Send { path, data_dir, listen } => {
            run_send(path, data_dir, listen);
        }
        Commands::Receive { ticket, dest, data_dir } => {
            if let Err(e) = std::fs::create_dir_all(&dest) {
                eprintln!("Failed to create destination directory '{}': {}", dest, e);
                std::process::exit(1);
            }
            run_receive(ticket, dest, data_dir);
        }
    }
}

fn run_send(path: String, data_dir: String, listen: bool) {
    let core = Core::new(data_dir).expect("Failed to initialize Seyfr core");
    println!("Node ID: {}", core.node_id());
    println!("Sending: {}", path);
    println!("Generating ticket...\n");

    match core.send(path) {
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

fn run_receive(ticket: String, dest: String, data_dir: String) {
    let core = Core::new(data_dir).expect("Failed to initialize Seyfr core");
    println!("Node ID: {}", core.node_id());
    println!("Receiving to: {}\n", dest);

    match core.receive(ticket, dest) {
        Ok(_) => {
            println!("\nTransfer complete.");
        }
        Err(e) => {
            eprintln!("Receive failed: {}", e);
            std::process::exit(1);
        }
    }
}
