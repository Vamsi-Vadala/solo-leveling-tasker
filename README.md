# Productivity App - Beta Release

## Overview
This app is designed to help users dedicate their time productively to important tasks rather than scrolling through social media. The concept is inspired by *Solo Leveling*, where failure to complete tasks results in restricted access to selected apps. While the app cannot yet verify task completion, it relies on user integrity. Users can bypass the restriction by uninstalling or force stopping the app, but future updates aim to enhance enforcement.

## Features
### 1. Daily Task Management
- Users are assigned a set number of tasks each day.
- Future updates will allow users to define and modify their own tasks.
- Currently, placeholder tasks are included as examples.

### 2. Task Completion Tracking
- Users can check/uncheck tasks for the current day.
- This ensures users track their productivity throughout the day.

### 3. Internet Restriction Mechanism
- At **12:01 AM**, the app checks whether all tasks for the previous day are completed.
- If **all tasks are completed**, internet access remains **unrestricted** the next day.
- If **tasks are incomplete**, internet access for **selected apps** will be **blocked**.

### 4. Customizable App Blocking
- Users can manually select which apps will be blocked in case of task failure.
- Important applications (e.g., banking, communication) can be exempted from blocking.

## How It Works
1. Install and launch the app.
2. Define the list of apps you want to restrict in case of incomplete tasks.
3. Check the list of daily tasks and complete them before **12:00 AM**.
4. If all tasks are completed, you retain internet access for the following day.
5. If tasks are incomplete by **12:01 AM**, the selected apps will be blocked.
6. The cycle repeats daily.

## Future Enhancements
- **Custom Task Addition**: Users will be able to create their own tasks.
- **Stronger Enforcement**: Methods to prevent easy circumvention (uninstall/force stop workarounds).
- **Personalized AI Assistance**: Leveraging LLMs to provide tailored productivity recommendations.
- **Adaptive "System" Integration**: Introducing a structured system based on user preferences and fields of interest.

## Installation Guide
1. Download the latest APK from the [Releases](https://github.com/Vamsi-Vadala/solo-leveling-tasker/releases/tag/v0.1.0-preview) section.
2. Install the APK on your Android device (ensure "Install from Unknown Sources" is enabled).
3. Open the app and configure your task and app restriction settings.

## Important Notes
- Ensure you do not block essential applications required for work or emergency purposes.
- The app currently **does not verify** actual task completion; it relies on the user’s honesty.
- Future updates will enhance enforcement to prevent easy bypassing.

## License
This project is open-source and distributed under the [MIT License](LICENSE).

## Feedback & Contributions
We welcome feedback and contributions! If you encounter any issues or have feature suggestions, feel free to open an issue or submit a pull request.

---
*This is an early beta release, and many features are still under development. Your feedback is valuable for refining and improving the app!*
