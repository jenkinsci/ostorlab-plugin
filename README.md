# Ostorlab Security And Privacy Scanner Plugin

Easily integrate security and privacy testing into your mobile application pipeline builds using the Ostorlab Jenkins
Plug-in. Using this plugin you
can upload Android and iOS applications and perform static (statically analyze the application without a test device), dyanmic (run
and assess the application on real device) and backend (assess backend interaction) scans.

## Prerequisites

- An account at the [Ostorlab Mobile application Scanner](https://report.ostorlab.co/account/login)
- Either use the free plan or add the desired [scan plans](https://report.ostorlab.co/plans) to your account  

## Usage

### Generate an API key

1. Go to the [API keys menu](https://report.ostorlab.co/library/api/keys) 
2. Click the new button to generate a new key
3. Copy the api key (You can add a name and an expiry date to your key)
4. Click the save button to save your key

![Api key Step1](https://github.com/amine3/ostorlab-plugin/tree/master/images/jenkins-apikey.png)

### Add Ostorlab's API key to Jenkins Credentials

1. From the main Jenkins dashboard, click the **Credentials** link.
    
    ![Api key Step1](https://github.com/amine3/ostorlab-plugin/tree/master/images/jenkins1.png)

2. Add new global credentials.
    -   In the **Kind** drop-down list, select **Secret text**.
    -   Enter **apiKey** in the ID field 
    -   Enter your API key in the Secret field.
    -   Enter a description to identify the key 

![Api key Step1](https://github.com/amine3/ostorlab-plugin/tree/master/images/jenkins3.png)

### Define Jenkins Job

![Api key Step1](https://github.com/amine3/ostorlab-plugin/tree/master/images/jenkins4.png)

1.  Add a **Secret text** binding to your Jenkins project configuration and enter the following information:
    
    ![Api key Step1](https://github.com/amine3/ostorlab-plugin/tree/master/images/jenkins5.png)
    ![Api key Step1](https://github.com/amine3/ostorlab-plugin/tree/master/images/jenkins6.png)
    ![Api key Step1](https://github.com/amine3/ostorlab-plugin/tree/master/images/jenkins7.png)
    -   **Variable:** Enter the name **apiKey**
    -   **Credentials:** Select specific credentials and choose the one defined in step 1

2.  Add a **Run Ostorlab Security Scanner** build step to your Jenkins project configuration and enter the following information:
    
    ![Api key Step1](/home/asasas333_3/IdeaProjects/oplugin/oplugin/images/jenkins8.png)
    ![Api key Step1](/home/asasas333_3/IdeaProjects/oplugin/oplugin/images/jenkins9.png)
    -   **File Path:** Enter the full path to the mobile application file that you want to scan. 

3. Click on Advanced settings to configure your run: 
    -   **Title:** Enter the mobile application path
    -   **Platform**: Select whether the platform is Android or iOS
    -   **Plan**: Select the type of the scan, SAST, DAST or SAST+DAST+BACKEND 
    -   **Wait for Results**: Suspend job until security analysis completes or times out
    -   **Max Wait Time (in minutes)**: Duration to wait before the job times out 
    -   **Break Build on higher Security Risk threshold**: If selected, the Jenkins job will fail if the findings risk equals or exceeds the specified thresholds (see below).
    -   **Security Risk Threshold**: Minimum Risk threshold that will cause a build to fail
![Api key Step1](https://github.com/amine3/ostorlab-plugin/tree/master/images/jenkins10.png)

4. Kick off build
   Kick off your mobile builds and you will see the scan risk in the artifacts folder.
![Api key Step1](https://github.com/amine3/ostorlab-plugin/tree/master/images/jenkins11.png)
