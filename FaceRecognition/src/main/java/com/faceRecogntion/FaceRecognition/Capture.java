package com.faceRecogntion.FaceRecognition;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.RectVector;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

public class Capture {
	public static void main(String[] args)  {
		
	}
	public void capture() throws Exception, InterruptedException{
		
		OpenCVFrameConverter.ToMat convertMat = new OpenCVFrameConverter.ToMat(); // convert image to Mat
		OpenCVFrameGrabber camera1 = new OpenCVFrameGrabber(0); // capturing webcam images

		camera1.start();
		
		String basePath=System.getProperty("user.dir");

		CascadeClassifier faceDetector = new CascadeClassifier(basePath+"\\src\\main\\resources\\haarcascade_frontalface_alt.xml");
		CanvasFrame cFrame = new CanvasFrame("Capture", CanvasFrame.getDefaultGamma() / camera1.getGamma()); // drawing
																												// a
																												// window
		Frame capturedFrame = null; // object to the captured frame
		Mat colorImage = new Mat(); // transfer from frame to color image for face detection
		int sampleNumber = 30;
		int sample = 1;

		Path path=Paths.get(basePath+"\\src\\main\\resources\\namedata.csv");
		
		String name = JOptionPane.showInputDialog("Enter Face Name");
		
		String personId = JOptionPane.showInputDialog("Enter Face ID");
		
		HashMap<String,String> data=new HashMap<String,String>();
		data.put("ID",personId);
		data.put("Name", name);
		try {
			FileWriter writer = new FileWriter(basePath+"\\src\\main\\resources\\namedata.csv",true);
			BufferedWriter bw=new BufferedWriter(writer);
			bw.append(data.get("ID").toString());
			bw.append(",");
			bw.append(data.get("Name").toString());
			bw.newLine();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Frame frame=null;
		Mat colorImg=new Mat();
		Mat gray=new Mat();

		while ((capturedFrame = camera1.grab()) != null && cFrame.isVisible()) 
		{
				while(sample<=sampleNumber)
				{
					frame=camera1.grab();
					colorImg=convertMat.convert(frame);
					opencv_imgproc.cvtColor(colorImg, gray, opencv_imgproc.COLOR_BGRA2GRAY); // convert image to gray for better detection
					RectVector detecFace = new RectVector(); // store detected faces
					faceDetector.detectMultiScale(gray, detecFace, 1.1, 1, 0, new Size(150, 150), new Size(500, 500));
					Rect faceD = detecFace.get(0);
					Mat captface = new Mat(gray, faceD);
					opencv_imgproc.resize(captface, captface, new Size(160, 160));
					opencv_imgcodecs.imwrite(basePath+"\\src\\main\\resources\\photos\\"+name+"."+personId+"."+sample+".jpg",captface);
					System.out.println("Photo " + sample + " captured\n");
					sample++;
				}
				
				if(sample>sampleNumber || !(cFrame.isVisible()))
				{
					cFrame.dispose(); // free memory
					camera1.stop();
					camera1.close();
					colorImg.close();
					colorImage.close();
					faceDetector.close();
					break;
				}

			if (cFrame.isVisible()) {
				cFrame.showImage(capturedFrame);
			}
		}
	}
}
