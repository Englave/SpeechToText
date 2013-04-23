import javax.sound.sampled.AudioInputStream;

/**
 * The heart of the algorithm. 
 * We need to normilize sound by using
 * pre_emphasise method. Then multiply result on WindowFunc
 * and start FFT.
 * After that we can create mel coefficients and recognize the word.
 * 
 * @author alex.zaiats
 *
 */
public class AudioInfo {
	private static final int NUM_BITS_PER_BYTE = 8;

	private AudioInputStream audioInputStream;
	private int[][] samplesContainer;
	private byte[] bytes;
	// cached values
	protected int sampleMax = 0;
	protected int sampleMin = 0;
	protected double biggestSample;
	double[] outreal;
	double[] outimag;

	public AudioInfo(AudioInputStream aiStream) {
		this.audioInputStream = aiStream;
		createSampleArrayCollection();
	}

	public int getNumberOfChannels() {
		int numBytesPerSample = audioInputStream.getFormat()
				.getSampleSizeInBits() / NUM_BITS_PER_BYTE;
		return audioInputStream.getFormat().getFrameSize() / numBytesPerSample;
	}

	private void createSampleArrayCollection() {
		try {
			audioInputStream.mark(Integer.MAX_VALUE);
			audioInputStream.reset();
			byte[] bytes = new byte[(int) (audioInputStream.getFrameLength())
					* ((int) audioInputStream.getFormat().getFrameSize())];

			double[] df = new double[bytes.length];
			double[] bt = new double[bytes.length];
			for (int i = 0; i < bt.length; i++)
				bt[i] = 0;
			long first_time = System.currentTimeMillis();
			df = pre_emphasise(bytes);

			df = four1(WindowFunc(df), df.length / 4, 1);
			samplesContainer = getSampleArray(df);

			// fr_test(df);

			double[] res = new double[bytes.length];
			res = make_mel_frequency(df);
			double[] mel = make_mel(res);
			long second_time = System.currentTimeMillis();
			System.out.println("Time  :  " + (second_time - first_time));

			if (sampleMax > sampleMin) {
				biggestSample = sampleMax;
			} else {
				biggestSample = Math.abs(((double) sampleMin));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void fr_test(double[] ar) {
		int k = 50;
		double[] mel = new double[k];

		int coef = Math.abs(ar.length / k);
		int n = 0;
		int len_of_arr = coef;

		for (int i = 0; i < k; i++) {

			for (; n < len_of_arr; n++) {
				mel[i] += ar[n];

			}

			len_of_arr += coef;
			if (len_of_arr > ar.length)
				len_of_arr = ar.length;
			mel[i] = mel[i] / coef;

		}
		for (int i = 0; i < k; i++)
			System.out.println("" + mel[i]);

		dwt(mel);
	}

	protected int[][] getSampleArray(byte[] eightBitByteArray) {
		int[][] toReturn = new int[getNumberOfChannels()][eightBitByteArray.length
				/ (2 * getNumberOfChannels())];
		int index = 0;

		// loop through the byte[]
		for (int t = 0; t < eightBitByteArray.length;) {
			// for each iteration, loop through the channels
			for (int a = 0; a < getNumberOfChannels(); a++) {
				// do the byte to sample conversion
				// see AmplitudeEditor for more info
				int low = (int) eightBitByteArray[t];
				t++;
				int high = (int) eightBitByteArray[t];
				t++;
				int sample = (high << 8) + (low & 0x00ff);

				if (sample < sampleMin) {
					sampleMin = sample;
				} else if (sample > sampleMax) {
					sampleMax = sample;
				}
				// set the value.
				toReturn[a][index] = sample;
			}
			index++;
		}

		return toReturn;
	}

	protected int[][] getSampleArray(double[] eightBitByteArray) {
		int[][] toReturn = new int[getNumberOfChannels()][eightBitByteArray.length
				/ (2 * getNumberOfChannels())];
		int index = 0;

		// loop through the byte[]
		for (int t = 0; t < eightBitByteArray.length;) {
			// for each iteration, loop through the channels
			for (int a = 0; a < getNumberOfChannels(); a++) {
				// do the byte to sample conversion
				// see AmplitudeEditor for more info
				int low = (int) eightBitByteArray[t];
				t++;
				int high = (int) eightBitByteArray[t];
				t++;
				int sample = (high << 8) + (low & 0x00ff);

				if (sample < sampleMin) {
					sampleMin = sample;
				} else if (sample > sampleMax) {
					sampleMax = sample;
				}
				// set the value.
				toReturn[a][index] = sample;
			}
			index++;
		}

		return toReturn;
	}

	public double getXScaleFactor(int panelWidth) {
		return (panelWidth / ((double) samplesContainer[0].length));
	}

	public double getYScaleFactor(int panelHeight) {
		return (panelHeight / (biggestSample * 2 * 1.2));
	}

	public int[] getAudio(int channel) {
		return samplesContainer[channel];
	}

	protected int getIncrement(double xScale) {
		try {
			int increment = (int) (samplesContainer[0].length / (samplesContainer[0].length * xScale));
			return increment;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public byte[] getBytes() {
		return bytes;
	}

	private static int log2(double x) {
		// Math.log is base e, natural log, ln
		return (int) (Math.log(x) / Math.log(2));
	}
	
	/**
	 * window function to normilize audio signal.
	 * 
	 * @param ar
	 * @return
	 */
	public static double[] WindowFunc(double[] ar) {
		// Heming window
		double[] db = new double[ar.length];
		for (int i = 0; i < ar.length; i++)
			db[i] = ar[i]
					* (0.53836 - 0.46164 * Math.cos(2 * Math.PI * (i - 1)
							/ (ar.length - 1)));

		return db;
	}
	
	/**
	 * create mel frequency to use mel cepstral algorithm.
	 * 
	 * @param ar
	 * @return
	 */
	public double[] make_mel_frequency(double[] ar) {
		int len = ar.length;
		double[] result = new double[len];
		for (int i = 0; i < len; i++) {
			result[i] = 1127 * Math.log(1 + ar[i] / 700);
		}
		return result;
	}
	
	/**
	 * creating mel array. 
	 * this array will used to recognize word.
	 * 
	 * @param ar
	 * @return
	 */
	public double[] make_mel(double[] ar) {
		int k = 50;
		double[] mel = new double[k];

		double elem = 0;
		int coef = Math.abs(ar.length / k);
		int n = 0;
		int num = 0;
		int len_of_arr = coef;

		for (int i = 0; i < k; i++) {

			for (; n < len_of_arr; n++) {
				elem += log2(ar[n]) * Math.abs(i * (num - 1 / 2) * Math.PI / k);
				num++;
			}

			mel[i] = elem / coef;
			len_of_arr += coef;
			if (len_of_arr > ar.length) {
				coef = len_of_arr - ar.length;
				len_of_arr = ar.length;
			}

			elem = 0;
			num = 0;
		}
		for (int i = 0; i < k; i++)
			System.out.println("" + mel[i]);
		dwt(mel);
		return mel;
	}

	/**
	 * The fast Fourier transform is a mathematical method for transforming a
	 * function of time into a function of frequency. Sometimes it is described
	 * as transforming from the time domain to the frequency domain. It is very
	 * useful for analysis of time-dependent phenomena.
	 * 
	 * @param data
	 * @param nn
	 * @param isign
	 * @return
	 */
	public double[] four1(double data[], int nn, int isign) {

		int i, j, n, mmax, m, istep;
		double wtemp, wr, wpr, wpi, wi, theta, tempr, tempi;

		n = nn << 1;
		j = 1;
		for (i = 1; i < n; i += 2) {
			if (j > i) {
				double temp;
				temp = data[j];
				data[j] = data[i];
				data[i] = temp;
				temp = data[j + 1];
				data[j + 1] = data[i + 1];
				data[i + 1] = temp;
			}
			m = n >> 1;
			while (m >= 2 && j > m) {
				j -= m;
				m >>= 1;
			}
			j += m;
		}
		mmax = 2;
		while (n > mmax) {
			istep = (mmax << 1);
			theta = isign * (6.28318530717959 / mmax);
			wtemp = Math.sin(0.5 * theta);
			wpr = -2.0 * wtemp * wtemp;
			wpi = Math.sin(theta);
			wr = 1.0;
			wi = 0.0;
			for (m = 1; m < mmax; m += 2) {
				for (i = m; i <= n; i += istep) {
					j = i + mmax;
					tempr = wr * data[j] - wi * data[j + 1];
					tempi = wr * data[j + 1] + wi * data[j];
					data[j] = data[i] - tempr;
					data[j + 1] = data[i + 1] - tempi;
					data[i] += tempr;
					data[i + 1] += tempi;
				}
				wr = (wtemp = wr) * wpr - wi * wpi + wr;
				wi = wi * wpr + wtemp * wpi + wi;
			}
			mmax = istep;
		}

		return data;
	}

	/**
	 * first audio processing. normalizes sound
	 * 
	 * @param ar
	 * @return
	 */
	public double[] pre_emphasise(byte[] ar) {
		int len = ar.length;
		double[] res = new double[len];
		res[0] = ar[0];
		for (int i = 1; i < len; i++) {
			res[i] = ar[i] - 0.9375 * ar[i - 1];
		}
		return res;
	}

	/**
	 * create complex array
	 * 
	 * @param ar
	 * @return Complex array
	 */
	public Complex[] make_complex(double[] ar) {
		int len = ar.length;
		Complex[] res = new Complex[len];
		for (int i = 0; i < len; i++)
			res[i] = new Complex(ar[i], 0);
		return res;
	}

	/**
	 * compare your word with constant word from Const class.
	 * 
	 * @param ar
	 *            array of mels
	 */
	public void dwt(double[] ar) {
		int len = ar.length;
		double chance = 0;
		double[] privet = Const.mel_yolka;
		for (int i = 2; i < len - 1; i++) {
			double tmp = Math.abs(ar[i] / privet[i]);
			if (tmp > 1)
				chance += Math.abs(privet[i] / ar[i]);
			else
				chance += Math.abs(ar[i] / privet[i]);

		}

		chance = chance / len;

		System.out.println("Chance Yolka   " + Math.abs(chance));

		chance = 0;
		privet = Const.mel_habar;

		for (int i = 2; i < len - 1; i++) {
			double tmp = Math.abs(ar[i] / privet[i]);
			if (tmp > 1)
				chance += Math.abs(privet[i] / ar[i]);
			else
				chance += Math.abs(ar[i] / privet[i]);

		}

		chance = chance / len;

		System.out.println("Chance habar   " + Math.abs(chance));

		chance = 0;
		privet = Const.cor_test;

		for (int i = 2; i < len - 1; i++) {
			double tmp = Math.abs(ar[i] / privet[i]);
			if (tmp > 1)
				chance += Math.abs(privet[i] / ar[i]);
			else
				chance += Math.abs(ar[i] / privet[i]);

		}

		chance = chance / len;

		System.out.println("Chance Corupt   " + Math.abs(chance));

	}
}