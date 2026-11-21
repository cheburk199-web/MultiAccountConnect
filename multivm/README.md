# Masterme4a MultiVM — prototype plan

This folder tracks a separate prototype for a three-profile Android app manager.

## Goal
- Purple/dark Android interface.
- Three named profile slots.
- Install/launch MJLauncher only where the Android platform and MJLauncher permit it.
- Never claim profile isolation unless it is verified on-device.

## Important platform limitation
A normal Android application cannot create arbitrary isolated Android users or run another app in three independent sandboxes. Starting an exported activity with `FLAG_ACTIVITY_NEW_TASK` or `FLAG_ACTIVITY_MULTIPLE_TASK` does not clone its package data, process, or account storage.

True isolation generally requires one of:
- Android managed/work profiles created through supported device-owner/profile-owner APIs;
- OEM-supported app cloning;
- a full virtualization/container engine with compatible Android framework integration.

A third-party app cannot silently provision a managed profile or bypass Android's app sandbox. This prototype must not request root, IMEI changes, credential interception, or account-token extraction.

## Milestones
1. Profile-manager UI and local profile metadata.
2. Capability check for supported Android managed-profile APIs / installed MJLauncher.
3. Proof-of-concept on the target TECNO device.
4. Only after independent storage is verified, add per-profile install/launch controls.

## Acceptance tests
- Each profile displays a distinct label.
- Installing MJLauncher in one profile does not make its private data visible to another.
- Login state remains separate after force-stop/reboot.
- Two instances can remain active only if the OS and launcher support it.
- If any test fails, show a clear unsupported status instead of a fake success.

## Current status
Planning/prototype only. No isolated MJLauncher instances have been implemented or verified yet.
