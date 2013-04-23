import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.sound.sampled.AudioInputStream;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;

public class WaveformPanelContainer extends JPanel {
    private ArrayList singleChannelWaveformPanels = new ArrayList();
    private AudioInfo audioInfo = null;
    double[] outreal;
    double[] outimag;
    public WaveformPanelContainer() {
        setLayout(new GridLayout(0,1));
    }

    public void setAudioToDisplay(AudioInputStream audioInputStream){
        singleChannelWaveformPanels = new ArrayList();
        audioInfo = new AudioInfo(audioInputStream);
        for (int t=0; t<audioInfo.getNumberOfChannels(); t++){
            SingleWaveformPanel waveformPanel
                    = new SingleWaveformPanel(audioInfo, t);
   /*		AudioDataBuffer.out("firie start " + audioInfo.getBytes().length);
            
            int len = audioInfo.getBytes().length;
            byte [] bt = audioInfo.getBytes();
            
            double[] arr = new double [bt.length];
            arr = WindowFunc(bt);
            AudioDataBuffer.out("Start");
      //      double[] awr = dft(arr);
        
       */
/*
            for (int i = 0 ; i < len ; i++)
            {
            	double sum = 0 ;
            	for (int k = 0 ; k < len ; k++)
            		sum += bt[i]*Math.pow(Math.E, -2*Math.PI*k*i/len);
            	cx[i] = sum;
            }
  */        
            
            
            singleChannelWaveformPanels.add(waveformPanel);
            add(createChannelDisplay(waveformPanel, t));
        }
    }

    private JComponent createChannelDisplay(SingleWaveformPanel waveformPanel, int index) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(waveformPanel, BorderLayout.CENTER);

        JLabel label = new JLabel("Channel " + ++index);
        panel.add(label, BorderLayout.NORTH);

        return panel;
    }

    	void dft(double[] inreal, double[] inimag) {
        int n = inreal.length;
        outreal = new double[n];
        outimag = new double[n];
        for (int k = 0; k < n; k++) {  // For each output element
            double sumreal = 0;
            double sumimag = 0;
            for (int t = 0; t < n; t++) {  // For each input element
                sumreal +=  inreal[t]*Math.cos(2*Math.PI * t * k / n) + inimag[t]*Math.sin(2*Math.PI * t * k / n);
                sumimag += -inreal[t]*Math.sin(2*Math.PI * t * k / n) + inimag[t]*Math.cos(2*Math.PI * t * k / n);
            }
            outreal[k] = sumreal;
            outimag[k] = sumimag;
        }
    }
    	
    	public static double[] WindowFunc (byte [] ar)
    	{
    		double [] db = new double [ar.length];
    		for (int i = 0 ; i < ar.length ; i++)
    			db[i] = ar[i] * (0.53836 - 0.46164*Math.cos(2*Math.PI*1/(ar.length-1)));
    		
    		return db;
    	}
    	
   	 public double[] dft(double v[]) { 
		   int N = v.length; 
		    
		   double t_img, t_real; 
		   double twoPikOnN; 
		   double twoPijkOnN; 
		   // how many bits do we need?  
		   N=log2(N);  
		   //Truncate input data to a power of two 
		   // length = 2**(number of bits). 
		    N = 1<<N;  
		     
		   double twoPiOnN = 2 * Math.PI / N; 
		   // We truncate to a power of two so that 
		   // we can compare execution times with the FFT. 
		   // DFT generally does not need to truncate its input. 
		   double [] r_data = new double [N];  
		   double [] i_data = new double [N]; 
		   double psd[] = new double[N]; 
		    
		   System.out.println("Executing DFT on "+N+" points..."); 
		   for(int k=0; k<N; k++) { 
		      twoPikOnN = twoPiOnN *k; 
		      for(int j = 0; j < N; j++) { 
		        twoPijkOnN = twoPikOnN * j; 
		         r_data[k] +=  v[j] * Math.cos( twoPijkOnN );
		         i_data[k] -=  v[j] * Math.sin( twoPijkOnN ); 
		      } 
		      r_data[k] /= N; 
		      i_data[k] /= N; 
		       
		      psd[k] =  
		       r_data[k] * r_data[k] +  
		       i_data[k] * i_data[k]; 
		   } 
		   return(psd); 
		 }
	 
	  private static int log2( int x )
    {
    // Math.log is base e, natural log, ln
    return  (int) (Math.log( x ) / Math.log( 2 ));
    }
    
}