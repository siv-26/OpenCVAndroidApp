**Bi-Directional Augmentative & Alternative Communication System for the Speech & Hearing Impaired**

![](RackMultipart20230424-1-i7rlcp_html_d7d2be9ed0422d6.png)

**Contributors to this project :**

ABHISHEK R MISHRA ( USN- 1MS16IS003 )

SIVA NANDAN REDDY RENDEDDULA ( USN- 1MS16IS097 )

VISHAL CHAVAN ( USN- 1MS16IS115 )

SANDHYA CHINNA PILLAI ( USN- 1MS16IS135 )

This project aims to bridge the communication gap between speech-impaired and abled individuals by providing a bi-directional communication system that supports both gesture translation and speech translation modes. The system uses computer vision and machine learning techniques to recognize Indian Sign Language (ISL) gestures captured by a camera and translate them into text, which is then output as speech for abled individuals. In the speech translation mode, the system converts spoken language into text for speech-impaired individuals to view.

**Motivation**

Hearing impairment affects a significant portion of the global population and leads to various consequences, including decreased communication ability, delay in language development, economic and educational backwardness, social isolation, and stigmatization. This project aims to provide a solution to these challenges by enabling smoother communication between speech-impaired and abled individuals.

**Requirements**

To run the system, you will need:

- A computer with a dedicated GPU for processing (e.g., NVIDIA GeForce GTX 1050 or higher)
- Python 3.6 or later
- OpenCV library
- Keras library
- English words datasets
- Text-to-speech and speech-to-text API modules
- Android Studio or any other 3rd party application for mobile platform integration

**Getting Started**

To get started with the project, follow these steps:

1. Clone the repository to your local machine.
2. Install the required libraries using pip or any other package manager.
3. Download the ISL dataset or create a new one from scratch.
4. Capture video using OpenCV and preprocess images to filter out the skin.
5. Train the CNN model using Keras and tune the hyperparameters for optimal performance.
6. Use words datasets to form words and sentences and present the result to users for validation.
7. Use text-to-speech and speech-to-text API modules to convert text to speech and vice versa.
8. Integrate the system into a mobile platform using Android Studio or any other 3rd party application.

**System Analysis and Design**

The system consists of two major components: Interface for Gesture Translation and Interface for Speech Translation (to text). The system analysis and design cover individual components and defining the Development Life Cycle (SDLC). The system characteristics include data collection of images, creating a dataset of Indian Hand Signs, and training the Convolution Neural Network (CNN) model.

![](RackMultipart20230424-1-i7rlcp_html_307ffcc05d20e89e.png)

**Gesture Translation Mode **

The Gesture Translation Mode is where the hand signs are mapped to their respective alphabets. The module offers Prediction by running the trained Convolution Neural Network (CNN) against the hand signs performed by the disabled person. The predicted alphabet is then displayed in a text view that has Text Completion capability so that the person may be able to select the most appropriate word suggestion that helps convey his/her message.

![](RackMultipart20230424-1-i7rlcp_html_7e6fd1066374bbc6.png)

**Image Preprocessing**

The image preprocessing steps involve resizing the picture to 120x120, converting it from RGB to YCrCb scale, defining a specific range of values that approximately define skin color, applying this range to obtain a mask where skin/gesture is identified as white pixels, and performing a bitwise\_and operation to obtain an image containing only the skin/gesture for training the model. This preprocessing is done to retain necessary elements of the picture and discard redundant elements, making it easier to process the input data for the CNN model.

**Model Generation**

The chosen model was generated along with many others on a trial and error basis, generating numerous models based on a combination of the following parameters - layer size, epochs, dense layers and convolution layers. This led to the creation of 81 models (3^4). Out of these models the most appropriate one was chosen which had high validation accuracy and minimal loss and did not over fit the data.

**F ![](RackMultipart20230424-1-i7rlcp_html_d0ccc3a6aaa33dde.png) ![](RackMultipart20230424-1-i7rlcp_html_adad4eed0d6dc576.png) ig. Image Preprocessing Stages Fig. Layers of the Chosen CNN Model**
