# MoodSensor

## Overview

**MoodSensor** is an Android application built to detect and classify facial emotions in real time. Using machine learning models and face detection techniques, the app identifies emotions such as **happiness**, **sadness**, **anger**, **surprise**, and **disgust** based on the user's facial expression. The app aims to provide a fun, interactive experience while showcasing emotion recognition capabilities within mobile technology.

The app is developed using **Java** in **Android Studio**, utilizing the **TensorFlow Lite** model for efficient machine learning inference. The app integrates the **CameraX API** to capture real-time facial data, which the model then processes to classify emotions accurately.

---

## Purpose

The purpose of this app is to:

- **Emotion Detection:** Use face detection to analyze the user's facial expression and predict the emotion using a pre-trained machine learning model.
- **Learning & Research:** Demonstrate a practical use of machine learning models on mobile devices, showcasing the capabilities of TensorFlow Lite and CameraX in real-time face recognition.
- **User Engagement:** Offer an interactive and fun experience where users can see how well the app detects emotions from their facial expressions.

---

## Features

- **Real-Time Emotion Detection:** Detects emotions such as happiness, sadness, anger, surprise, and disgust directly from the user's face.
- **Camera Integration:** Uses CameraX for real-time facial image capture.
- **TensorFlow Lite Integration:** A pre-trained TensorFlow Lite model classifies the emotions based on the detected facial expressions.
- **Efficient Mobile Usage:** The app is optimized for mobile devices, ensuring smooth and quick emotion detection without draining device resources.
- **Java Implementation:** The app is entirely built in **Java** for Android Studio, excluding the need for Kotlin or other languages.

---

## Getting Started

To run the app on your Android device, follow these steps:

### Prerequisites

- **Android Studio** installed on your machine.
- A **physical Android device** or **emulator** to run the app.
- **TensorFlow Lite model** files integrated into the app (can be downloaded or used from the model directory).

### Steps to Build and Run:

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Daddyk5/MoodSensor.git
   ```
   
2. **Open the project in Android Studio.**
   
3. **Build the project:** 
   Click on **Build > Make Project** to ensure everything is compiled correctly.

4. **Run on a device or emulator:**
   - Make sure your device is connected via USB or your emulator is running.
   - Click the **Run** button in Android Studio to launch the app.

5. **Permissions:**
   - The app requires permission to use the camera. When prompted, grant the necessary permissions for the camera to function correctly.

---

## Model Information

The app uses a pre-trained **MobileNet model** for emotion detection, converted to **TensorFlow Lite** format to optimize it for mobile performance.

### Supported Emotions:
- **Happiness**
- **Sadness**
- **Anger**
- **Surprise**
- **Disgust**

---

## Technologies Used

- **Android Studio** - Integrated Development Environment for Android development.
- **Java** - Primary programming language for the app.
- **TensorFlow Lite** - Lightweight model optimized for running on mobile devices.
- **CameraX API** - For capturing real-time images of the user's face for emotion detection.
- **OpenCV** (if used) - For additional image processing or face detection.

---

## Contributing

If you would like to contribute to the development of this project, please fork the repository and submit a pull request. We welcome any suggestions or improvements!

### Steps to contribute:

1. Fork the repository.
2. Create a new branch (`git checkout -b feature-branch`).
3. Commit your changes (`git commit -am 'Add new feature'`).
4. Push to your branch (`git push origin feature-branch`).
5. Submit a pull request with a description of the changes.

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Acknowledgements

- **TensorFlow Lite:** For providing a powerful and lightweight model for emotion detection.
- **Google's CameraX API:** For offering a simple and efficient way to access and use camera features in Android apps.
- **MobileNet:** The pre-trained model used for emotion classification.

---

This **README** provides all the necessary details for users to understand, build, and contribute to the **MoodSensor** project.

You can adjust and expand the content based on your needs or any additional features in the app!
