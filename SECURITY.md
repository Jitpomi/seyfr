# Security Policy

Filesystems are the "Wild West" of application security. Without these guardrails, a multiplatform app—which has to juggle different file rules for Windows, macOS, and Linux—could accidentally give an attacker the keys to the entire operating system.

Here is why each of those features is a non-negotiable part of a secure app's DNA:

## 1. Path Traversal Protection (path_jail)

### The Threat

An attacker tries to "escape" the folder you've given them access to by using sequences like `../` (dot-dot-slash). If your app asks for a profile picture and the user provides `../../../../etc/shadow`, a vulnerable app might actually serve up the system's encrypted password file.

### The Solution

A `path_jail` (or chroot-like mechanism) forces the application to treat a specific directory as the "root." Even if a malicious path contains 50 "go up" commands, the app physically cannot see anything above its assigned sandbox.

## 2. File Size & Collection Limits

### The Threat: Denial of Service (DoS)

**Size Limits:** Without a 1 GB limit, a user could upload a "Zip Bomb" or a 500 GB file that fills your server's disk space, crashing the service for everyone.

**Collection Limits:** Processing 10,000+ files at once requires massive amounts of RAM and CPU to index and track. An uncapped transfer could cause the app to hang or crash due to memory exhaustion (OOM).

## 3. Destination Validation

### The Threat

Even without "traversal" tricks, an app might be tricked into writing files to sensitive locations it technically has permission to access, like a startup folder or a shared library directory.

### The Solution

This ensures the app explicitly checks, "Is this specific folder on my 'Allowed' list?" before it touches the disk. It's the difference between a bouncer checking your ID and a bouncer checking if you're even on the guest list.

## 4. Symlink Safety

### The Threat

Symbolic links (symlinks) are "shortcuts." An attacker can include a symlink in a transfer that looks like a harmless text file but actually points to `/etc/passwd` or `C:\Windows\System32`. If the app follows that link during a write operation, it might overwrite a critical system file.

### The Solution

By skipping symlinks by default, the app refuses to follow these "redirects," ensuring it only interacts with the actual data provided, not pointers to the rest of the machine.

---

## Summary of Protections

| Feature | Primary Goal | Prevents... |
| --- | --- | --- |
| Path Jail | Containment | Accessing OS system files |
| Size Limits | Resource Stability | Disk space exhaustion (DoS) |
| Collection Limits | Performance | Memory crashes / App hanging |
| Destination Validation | Integrity | Overwriting unintended directories |
| Symlink Safety | Escape Prevention | "Zip Slip" and shortcut-based exploits |

---

## Current Limits

```rust
const MAX_FILE_SIZE: u64 = 1_073_741_824;           // 1 GB per file
const MAX_COLLECTION_SIZE: u64 = 10_737_418_240;    // 10 GB per collection
const MAX_FILES_IN_COLLECTION: u64 = 10_000;        // Max 10,000 files
```

## Reporting a Vulnerability

If you discover a security vulnerability in Seyfr, please report it responsibly. We take security seriously and will work to address issues promptly.

[Add your preferred reporting mechanism here - e.g., email, GitHub Security Advisories, etc.]
