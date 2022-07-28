package com.faceRecogntion.FaceRecognition;

import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.RectVector;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
 
class Key extends KeyAdapter{
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode()==KeyEvent.VK_ESCAPE) {
			System.exit(0);
		}
	}
}
public class Recognition {
	public static void main(String[] args) {
		Recognition ob=new Recognition();
		try {
			ob.recog();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	static String getValue(HashMap<String,String> data,int Selection)
	{
			return data.get(Integer.toString(Selection));
	}
	public void recog() throws Exception, InterruptedException {
		
		OpenCVFrameConverter.ToMat convertMat = new OpenCVFrameConverter.ToMat(); // convert image to Mat
		OpenCVFrameGrabber camera1 = new OpenCVFrameGrabber(0); // capturing webcam images
		
		
		ArrayList<String> people=new ArrayList<>(30);
		HashMap<String,String> data = new HashMap<String, String>();
		String basePath=System.getProperty("user.dir");
		Path path=Paths.get(basePath+"\\src\\main\\resources\\namedata.csv");
		try {
			List<String> list=Files.readAllLines(path);
			for(String lis:list) {
				//people.add(Integer.parseInt(lis.substring(lis.length()-1)), lis.substring(0, lis.length()-1));
				data.put(lis.split(",")[0].toString(), lis.split(",")[1].toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		camera1.start();
		
		CascadeClassifier faceDetector = new CascadeClassifier(basePath+"\\src\\main\\resources\\haarcascade_frontalface_alt.xml");
		
		FaceRecognizer recognizer = createLBPHFaceRecognizer();
		recognizer.load(basePath+"\\src\\main\\resources\\classifierLBPH.yml");
		recognizer.setThreshold(65.0);
		
		CanvasFrame cFrame = new CanvasFrame("Recognition", CanvasFrame.getDefaultGamma() / camera1.getGamma());// drawing a window
		
		Frame capturedFrame = null; // object to the captured frame
		Mat colorImage = new Mat(); // transfer from frame to color image for face detection
		int choice=-1;
		
		cFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);			
			}
		});
		while ((capturedFrame = camera1.grab()) != null ) 
		{
			cFrame.addKeyListener(new Key());
			colorImage = convertMat.convert(capturedFrame);
			Mat grayImage = new Mat();
			opencv_imgproc.cvtColor(colorImage, grayImage, opencv_imgproc.COLOR_BGRA2GRAY); // convert image to gray for better detection
			RectVector detectedFaces = new RectVector(); // store detected faces
			faceDetector.detectMultiScale(grayImage, detectedFaces, 1.1, 1, 0, new Size(150, 150), new Size(500, 500));

			for (int i = 0; i < detectedFaces.size(); i++) // cycle detected faces vector
			{ 
				Rect faceData = detectedFaces.get(i);
				opencv_imgproc.rectangle(colorImage, faceData, new Scalar(0, 0, 255, 0)); // insert rectangle in color image
																							
				Mat capturedface = new Mat(grayImage, faceData);
				opencv_imgproc.resize(capturedface, capturedface, new Size(160, 160));
				
				IntPointer label = new IntPointer(1); // identify the image label
				DoublePointer confidence = new DoublePointer(1); 
				recognizer.predict(capturedface, label, confidence); // will classify the new image according to the training
				int selection = label.get(0); // choice made by the classifier
				String name;
				if (selection == -1) 
				{
					name="Unknown";
				} else 
				{
					name= getValue(data,selection) + " - " + confidence.get(0);
				}
				
				// entering the name on the screen
				int x = Math.max(faceData.tl().x() -10, 0); 
				int y = Math.max(faceData.tl().y() -10, 0);
				opencv_imgproc.putText(colorImage, name, new Point(x,y), opencv_core.FONT_HERSHEY_PLAIN, 1.4, new Scalar(0,255,0,0));
				if(!(cFrame.isVisible()) && choice==0)
				{
					cFrame.dispose(); // free memory
					camera1.stop();
					camera1.close();
					colorImage.close();
					faceDetector.close();
					System.exit(0);
				}
			}

			if (cFrame.isVisible()) {
				cFrame.showImage(capturedFrame);
			}
		}

	}
}