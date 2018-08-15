#!/bin/sh

if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root" 1>&2
   exit 1
fi

for KERNEL in /boot/vmlinuz-*; do
    grubby --update-kernel="$KERNEL" --args='transparent_hugepage=never'
done