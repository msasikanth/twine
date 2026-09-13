# Release signing

Encrypted Android release keystore for CI:

- File: `reader.gpg` (AES256-symmetric via gpg)
- Decrypts to `reader.jks` in the workflow
- Key alias must be `reader_alias` (see `androidApp/build.gradle.kts`)

One-time setup (Actions enable + keystore + GitHub secrets):

```bash
./scripts/setup-github-releases.sh
```

Never commit a plaintext `.jks`.
