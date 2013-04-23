import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Main class of speech to text engine.
 * Form have only 1 button to take wav from computer.
 * After that will be created WaveForm of wav file and
 * calculated mel coefficients
 * 
 * @author alex.zaiats
 *
 */
public class AudioDataBuffer extends JFrame {
	/**
	 * version 1. Some bugs with long word recognition.
	 * Work only with 1 word in wav file!!!
	 */
	private static final long serialVersionUID = 1L;

	private JTextField input = new JTextField("", 5);

	private JButton open = new JButton("Open");

	public AudioDataBuffer() {
		super("Choose file");
		this.setBounds(50, 50, 50, 100);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container container = this.getContentPane();
		open.addActionListener(new OpenClass());
		container.add(open);

	}

	public static void main(String[] args) throws Exception {
		AudioDataBuffer app = new AudioDataBuffer();
		app.setVisible(true);

	}

	class OpenClass implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser("D://");
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					"WAV files", "wav");
			chooser.setFileFilter(filter);
			int option = chooser.showOpenDialog(AudioDataBuffer.this);
			if (option == JFileChooser.APPROVE_OPTION) {
				input.setText("You chose "
						+ ((chooser.getSelectedFile() != null) ? chooser
								.getSelectedFile().getAbsolutePath()
								: "nothing"));
				if (chooser.getSelectedFile() != null)
					try {

						JFrame frame = new JFrame("Waveform Display Simulator");
						frame.setBounds(200, 200, 500, 350);

						File file = new File(chooser.getSelectedFile()
								.getAbsolutePath());
						/*
						 * good working files : habar_alc habar corupt yolka
						 * marka priv_i
						 */
						AudioInputStream audioInputStream = AudioSystem
								.getAudioInputStream(new BufferedInputStream(
										new FileInputStream(file)));

						WaveformPanelContainer container = new WaveformPanelContainer();
						container.setAudioToDisplay(audioInputStream);

						frame.getContentPane().setLayout(new BorderLayout());
						frame.getContentPane().add(container,
								BorderLayout.CENTER);

						frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

						frame.show();
						frame.validate();
						frame.repaint();

					} catch (Exception ed) {
						ed.printStackTrace();
					}
			}

			if (option == JFileChooser.CANCEL_OPTION) {
				input.setText("You canceled.");
			}
		}
	}

}