package utils;

import java.awt.GraphicsEnvironment;
import java.awt.Taskbar;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

public class ImageArrGen {

	// Funny little inner variables
	private static InputStream[] outputter;

	// Utility class don't remove or access.
	private ImageArrGen() {
		throw new IllegalStateException("How TF did you get here");
	}

	// Call to change book, check main class for example
	public static InputStream[] imageToMem(String filePath) throws IOException {
		if (filePath.endsWith(".cb7")) {
			arbitrary7z(filePath);
		} else if (filePath.endsWith(".cbz")) {
			arbitrary(filePath);
		} else if (filePath.endsWith(".cbt")) {
			arbitraryTar(filePath);
		}

		return getOutputter();
	}

	// I fucking called it ARBITRARY for a reason, it does a bunch of random shit
	// NOTE that this method only works on standard zip files
	private static void arbitrary(String filePath) throws IOException {

		// Initialize ui values
		/* Don't use javaFX because it doesn't support taskbar loading bars and I don't
		understand javaFX ui threads*/
		JFrame f = new JFrame("Progress Bar");
		JPanel p = new JPanel();
		JProgressBar b = new JProgressBar();

		// Zip file to get inner file amount
		ZipFile zip = new ZipFile(filePath);
		float allPages = zip.size();

		/* Allocate array slots. Keep the plus one so theres a null to account for in
		 the continue button.*/
		outputter = new InputStream[(int) allPages + 1];

		// Set all UI variables
		b.setValue(0);
		b.setMinimum(0);
		b.setStringPainted(true);
		p.add(b);
		f.add(p);
		f.setSize(350, 70);
		f.setVisible(true);
		f.toFront();
		f.setResizable(false);
		f.setLocation(
				GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width / 2
						- (f.getWidth() / 2),
				GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height / 2
						- (f.getHeight() / 2));
		b.setMaximum((int) allPages);

		// Initialize the Taskbar loading bar.
		Taskbar taskbar = Taskbar.getTaskbar();
		taskbar.setWindowProgressState(f, Taskbar.State.NORMAL);

		// Deallocate the zip file.
		zip.close();

		// Initialize the fileInputStream
		try (FileInputStream file = new FileInputStream(Paths.get(filePath).toFile())) {

			// Initialize the zipInputStream
			try (ZipInputStream zipInput = new ZipInputStream(file)) {

				/* For loop for indexing all the pages. the zipInput.getNextEntry is also called
				 when used as a variable so don't call it again. */
				for (int i = 0; zipInput.getNextEntry() != null; i++) {
					// If page is bigger than 1 run
					if (i > 1) {
						// Set all progress indicators
						f.setTitle("Indexing pages: " + (i / allPages) * 100 + "%");
						taskbar.setWindowProgressValue(f, (int) ((i / allPages) * 100));
						b.setValue(i);
					}

					// OutputStream used to store the image and convert it to InputStream.
					ByteArrayOutputStream inout = new ByteArrayOutputStream();

					// Convert OutputStream to PNG.
					// try again in catch to account for folders/random files
					try {
						ImageIO.write(ImageIO.read(zipInput), "PNG", inout);
					}catch(IllegalArgumentException excp) {
						zipInput.getNextEntry();
						try {
							ImageIO.write(ImageIO.read(zipInput), "PNG", inout);
						}catch(IllegalArgumentException excp1) {
							zipInput.getNextEntry();
							try {
								ImageIO.write(ImageIO.read(zipInput), "PNG", inout);
							}catch(IllegalArgumentException excp2) {
								zipInput.getNextEntry();
								ImageIO.write(ImageIO.read(zipInput), "PNG", inout);
							}
						}
					}

					// Set outputter.
					getOutputter()[i] = new ByteArrayInputStream(inout.toByteArray());
				}
			} finally {
				// Destroy progress window
				f.dispose();
			}
		}
	}

	// Like arbitrary but for 7z archives
	private static void arbitrary7z(String filePath) throws IOException {
		// Initialize ui values
		/* Don't use javaFX because it doesn't support taskbar loading bars and I don't
		understand javaFX ui threads*/
		JFrame f = new JFrame("Progress Bar");
		JPanel p = new JPanel();
		JProgressBar b = new JProgressBar();

		// Zip file to get inner file amount
		SevenZFile zip = new SevenZFile(new File(filePath));
		float allPages = 0;
		while (zip.getNextEntry() != null) {
			allPages++;
		}

		/* Allocate array slots. Keep the plus one so theres a null to account for in
		 the continue button.*/
		outputter = new InputStream[(int) allPages + 1];

		// Set all UI variables
		b.setValue(0);
		b.setMinimum(0);
		b.setStringPainted(true);
		p.add(b);
		f.add(p);
		f.setSize(350, 70);
		f.setVisible(true);
		f.toFront();
		f.setResizable(false);
		f.setLocation(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width / 2 - (f.getWidth() / 2), GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height / 2 - (f.getHeight() / 2));
		b.setMaximum((int) allPages);

		// Initialize the Taskbar loading bar.
		Taskbar taskbar = Taskbar.getTaskbar();
		taskbar.setWindowProgressState(f, Taskbar.State.NORMAL);

		// Deallocate the zip file.
		zip.close();

		// Initialize the SevenZFile
		try (SevenZFile zipInput = new SevenZFile(new File(filePath))) {
			SevenZArchiveEntry entry;
			/* For loop for indexing all the pages. the zipInput.getNextEntry is also called
			 when used as a variable so don't call it again. */
			for (int i = 0; (entry = zipInput.getNextEntry()) != null; i++) {
				// If page is bigger than 1 run
				if (i > 1) {
					// Set all progress indicators
					f.setTitle("Indexing pages: " + (i / allPages) * 100 + "%");
					taskbar.setWindowProgressValue(f, (int) ((i / allPages) * 100));
					b.setValue(i);
				}

				// OutputStream used to store the image and convert it to InputStream.
				ByteArrayOutputStream inout = new ByteArrayOutputStream();

				// Convert OutputStream to PNG.
				// try again in catch to account for folders/random files
				try {
					ImageIO.write(ImageIO.read(zipInput.getInputStream(entry)), "PNG", inout);
				}catch(IllegalArgumentException excp) {
					entry = zipInput.getNextEntry();
					try {
						ImageIO.write(ImageIO.read(zipInput.getInputStream(entry)), "PNG", inout);
					}catch(IllegalArgumentException excp1) {
						entry = zipInput.getNextEntry();
						try {
							ImageIO.write(ImageIO.read(zipInput.getInputStream(entry)), "PNG", inout);
						}catch(IllegalArgumentException excp2) {
							entry = zipInput.getNextEntry();
							ImageIO.write(ImageIO.read(zipInput.getInputStream(entry)), "PNG", inout);
						}
					}
				}

				// Set outputter.
				getOutputter()[i] = new ByteArrayInputStream(inout.toByteArray());
			}
		} finally {
			// Destroy progress window
			f.dispose();
		}
	}

	// Like arbitrary but for tarballs
	private static void arbitraryTar(String filePath) throws IOException {
		// Initialize ui values
		/* Don't use javaFX because it doesn't support taskbar loading bars and I don't
		 understand javaFX ui threads */
		JFrame f = new JFrame("Progress Bar");
		JPanel p = new JPanel();
		JProgressBar b = new JProgressBar();

		// Zip file to get inner file amount
		TarArchiveInputStream zip = new TarArchiveInputStream(new FileInputStream(new File(filePath)));
		float allPages = 0;
		while (zip.getNextEntry() != null) {
			allPages++;
		}

		/* Allocate array slots. Keep the plus one so theres a null to account for in
		 the continue button. */
		outputter = new InputStream[(int) allPages + 1];

		// Set all UI variables
		b.setValue(0);
		b.setMinimum(0);
		b.setStringPainted(true);
		p.add(b);
		f.add(p);
		f.setSize(350, 70);
		f.setVisible(true);
		f.toFront();
		f.setResizable(false);
		f.setLocation(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width / 2 - (f.getWidth() / 2), GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height / 2 - (f.getHeight() / 2));
		b.setMaximum((int) allPages);

		// Initialize the Taskbar loading bar.
		Taskbar taskbar = Taskbar.getTaskbar();
		taskbar.setWindowProgressState(f, Taskbar.State.NORMAL);

		// Deallocate the zip file.
		zip.close();

		// Initialize the fileInputStream
		try (FileInputStream file = new FileInputStream(Paths.get(filePath).toFile())) {

			// Initialize the zipInputStream
			try (TarArchiveInputStream zipInput = new TarArchiveInputStream(file)) {

				/* For loop for indexing all the pages. the zipInput.getNextEntry is also called
				when used as a variable so don't call it again. */
				for (int i = 0; zipInput.getNextEntry() != null; i++) {
					// If page is bigger than 1 run
					if (i > 1) {
						// Set all progress indicators
						f.setTitle("Indexing pages: " + (i / allPages) * 100 + "%");
						taskbar.setWindowProgressValue(f, (int) ((i / allPages) * 100));
						b.setValue(i);
					}

					// OutputStream used to store the image and convert it to InputStream.
					ByteArrayOutputStream inout = new ByteArrayOutputStream();

					// Convert OutputStream to PNG.
					// try again in catch to account for folders/random files
					try {
						ImageIO.write(ImageIO.read(zipInput), "PNG", inout);
					}catch(IllegalArgumentException excp) {
						zipInput.getNextEntry();
						try {
							ImageIO.write(ImageIO.read(zipInput), "PNG", inout);
						}catch(IllegalArgumentException excp1) {
							zipInput.getNextEntry();
							try {
								ImageIO.write(ImageIO.read(zipInput), "PNG", inout);
							}catch(IllegalArgumentException excp2) {
								zipInput.getNextEntry();
								ImageIO.write(ImageIO.read(zipInput), "PNG", inout);
							}
						}
					}
					

					// Set outputter.
					getOutputter()[i] = new ByteArrayInputStream(inout.toByteArray());
				}
			} finally {
				// Destroy progress window
				f.dispose();
			}
		}
	}

	// Get it because it's private
	public static InputStream[] getOutputter() {
		return outputter;
	}
}
