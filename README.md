# ml-java-setup
Configure a cluster from scratch using Java and the ReST API

1. Disable THPs

```bash
#!/bin/sh

if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root" 1>&2
   exit 1
fi

for KERNEL in /boot/vmlinuz-*; do
    grubby --update-kernel="$KERNEL" --args='transparent_hugepage=never'
done
```


Work in progress

```
openssl rsa -in ~/.ssh/id_rsa -outform pem > id_rsa.pem
chmod 600 id_rsa.pem
```