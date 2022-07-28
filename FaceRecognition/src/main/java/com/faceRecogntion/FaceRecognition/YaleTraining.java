package com.faceRecogntion.FaceRecognition;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;

import javax.swing.JOptionPane;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacpp.opencv_imgproc;

import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;

public class YaleTraining {
    public static void main(String[] args) {
        
    }
    public void yaleTraining() {
    	File directory = new File("C:\\Users\\Abhash\\Documents\\eclipse-workspace\\faceRecognition\\src\\main\\resources\\faces\\training");
        FilenameFilter imageFilter = new FilenameFilter() {   // filter image type
			public boolean accept(File dir, String name) {
				return name.endsWith(".jpg") || name.endsWith(".gif") || name.endsWith(".png") ;
			}
		};
        File[] files = directory.listFiles(imageFilter);
        MatVector photos = new MatVector(files.length);
        Mat labels = new Mat(files.length, 1, opencv_core.CV_32SC1);
        IntBuffer bufferLabels = labels.createBuffer();
        int counter = 0;
      
        for (File image : files) {
            Mat photo = opencv_imgcodecs.imread(image.getAbsolutePath(), opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
            int personId = Integer.parseInt(image.getName().substring(5,10));
            opencv_imgproc.resize(photo, photo, new Size(160, 160));
            photos.put(counter, photo);
            bufferLabels.put(counter, personId);
            counter++;
        }
        
        //classifiers
        //FaceRecognizer eigenface = createEigenFaceRecognizer(30, 0);
        //FaceRecognizer fisherface = createFisherFaceRecognizer(30, 0);        
        FaceRecognizer lbph = createLBPHFaceRecognizer(12, 10, 15, 15, 0);

        
        // classifiers -> learning-training to generate yml codes
     	/*eigenface.train(photos, labels);
     	eigenface.save("src\\main\\java\\resources\\classifierEigenFacesYale.yml");
     		
     	fisherface.train(photos, labels);
     	fisherface.save("src\\main\\java\\resources\\classifierFisherFacesYale.yml");*/
     	
     	lbph.train(photos, labels);
     	lbph.save("C:\\Users\\Abhash\\Documents\\eclipse-workspace\\faceRecognition\\src\\main\\resources\\classifierLBPHYale.yml");
     	JOptionPane.showMessageDialog(null, "Training Complete!","FACE RECOGNITION",JOptionPane.INFORMATION_MESSAGE);
    }
}