import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.Point;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReconocimientoFacial {
    private static final String CASCADE_FILE = "haarcascade_frontalface_default.xml";
    private CascadeClassifier faceDetector;
    private List<Mat> rostrosRegistrados = new ArrayList<>();
    private List<String> nombresRostros = new ArrayList<>();

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public ReconocimientoFacial() {
        try {
            // Carga desde recursos
            InputStream is = getClass().getResourceAsStream("/haarcascade_frontalface_default.xml");
            if (is == null) {
                throw new FileNotFoundException("Archivo HAAR no encontrado en recursos");
            }

            // Crea archivo temporal
            File tempFile = File.createTempFile("haarcascade", ".xml");
            Files.copy(is, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            tempFile.deleteOnExit();

            faceDetector = new CascadeClassifier(tempFile.getAbsolutePath());

            if (faceDetector.empty()) {
                throw new Exception("El clasificador se cargó pero está vacío");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error al cargar el clasificador facial:\n" + e.getMessage() +
                            "\n\nAsegúrate que el archivo haarcascade_frontalface_default.xml" +
                            "\nesté en la carpeta src/resources/",
                    "Error Crítico", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    public void registrarRostro(String usuario) {
        JFrame frame = new JFrame("Registro Facial - " + usuario);
        frame.setSize(640, 480);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        JLabel lblImagen = new JLabel();
        panel.add(lblImagen);
        frame.add(panel);
        frame.setVisible(true);

        VideoCapture capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            JOptionPane.showMessageDialog(frame, "No se pudo acceder a la cámara");
            return;
        }

        Mat webcamImage = new Mat();
        JButton btnCapturar = new JButton("Capturar Rostro");
        panel.add(btnCapturar);

        btnCapturar.addActionListener(e -> {
            Mat rostro = detectarRostro(webcamImage);
            if (rostro != null) {
                rostrosRegistrados.add(rostro);
                nombresRostros.add(usuario);
                JOptionPane.showMessageDialog(frame, "Rostro registrado exitosamente");
                frame.dispose();
            } else {
                JOptionPane.showMessageDialog(frame, "No se detectó un rostro. Intente de nuevo.");
            }
        });

        // Hilo para mostrar la cámara en tiempo real
        new Thread(() -> {
            while (frame.isVisible()) {
                capture.read(webcamImage);
                if (!webcamImage.empty()) {
                    MatOfRect faceDetections = new MatOfRect();
                    faceDetector.detectMultiScale(webcamImage, faceDetections);

                    // Dibujar rectángulos alrededor de los rostros detectados
                    for (Rect rect : faceDetections.toArray()) {
                        Imgproc.rectangle(webcamImage, new Point(rect.x, rect.y),
                                new Point(rect.x + rect.width, rect.y + rect.height),
                                new Scalar(0, 255, 0), 3);
                    }

                    lblImagen.setIcon(new ImageIcon(matToBufferedImage(webcamImage)));
                }
            }
            capture.release();
        }).start();
    }

    public String reconocerRostro() {
        JFrame frame = new JFrame("Reconocimiento Facial");
        frame.setSize(640, 480);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        JLabel lblImagen = new JLabel();
        JLabel lblResultado = new JLabel("Esperando detección...");
        panel.setLayout(new BorderLayout());
        panel.add(lblImagen, BorderLayout.CENTER);
        panel.add(lblResultado, BorderLayout.SOUTH);
        frame.add(panel);
        frame.setVisible(true);

        VideoCapture capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            JOptionPane.showMessageDialog(frame, "No se pudo acceder a la cámara");
            return null;
        }

        // Usamos un array para almacenar el resultado (solución para la variable en lambda)
        final String[] usuarioReconocido = {null};

        // Hilo para reconocimiento en tiempo real
        new Thread(() -> {
            Mat webcamImage = new Mat();
            while (frame.isVisible() && usuarioReconocido[0] == null) {
                capture.read(webcamImage);
                if (!webcamImage.empty()) {
                    Mat rostro = detectarRostro(webcamImage);
                    if (rostro != null && !rostro.empty()) {
                        usuarioReconocido[0] = compararRostro(rostro);
                        if (usuarioReconocido[0] != null) {
                            SwingUtilities.invokeLater(() -> {
                                lblResultado.setText("Usuario reconocido: " + usuarioReconocido[0]);
                                JOptionPane.showMessageDialog(frame,
                                        "Usuario reconocido: " + usuarioReconocido[0]);
                                frame.dispose();
                            });
                        }
                    }

                    // Actualizar la imagen en el hilo de eventos de Swing
                    SwingUtilities.invokeLater(() -> {
                        lblImagen.setIcon(new ImageIcon(matToBufferedImage(webcamImage)));
                    });
                }
                try {
                    Thread.sleep(30); // Reducir carga de CPU
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            capture.release();
        }).start();

        // Esperar hasta que se cierre la ventana o se reconozca un rostro
        while (frame.isVisible() && usuarioReconocido[0] == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return usuarioReconocido[0];
    }

    private Mat detectarRostro(Mat imagen) {
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(imagen, faceDetections);

        if (faceDetections.toArray().length > 0) {
            Rect rect = faceDetections.toArray()[0];
            Mat rostro = new Mat(imagen, rect);
            Imgproc.resize(rostro, rostro, new Size(200, 200));
            return rostro;
        }
        return null;
    }

    private String compararRostro(Mat rostro) {
        if (rostrosRegistrados.isEmpty()) return null;

        double mejorSimilitud = Double.MAX_VALUE;
        int mejorIndice = -1;

        for (int i = 0; i < rostrosRegistrados.size(); i++) {
            Mat rostroRegistrado = rostrosRegistrados.get(i);
            double similitud = compararMatrices(rostro, rostroRegistrado);

            if (similitud < mejorSimilitud) {
                mejorSimilitud = similitud;
                mejorIndice = i;
            }
        }

        // Umbral de similitud (ajustable)
        if (mejorSimilitud < 0.6) {
            return nombresRostros.get(mejorIndice);
        }
        return null;
    }

    private double compararMatrices(Mat mat1, Mat mat2) {
        Mat hist1 = new Mat();
        Mat hist2 = new Mat();

        MatOfInt histSize = new MatOfInt(256);
        MatOfFloat ranges = new MatOfFloat(0f, 256f);
        MatOfInt channels = new MatOfInt(0);

        // Calcular histogramas
        Imgproc.calcHist(List.of(mat1), channels, new Mat(), hist1, histSize, ranges);
        Imgproc.calcHist(List.of(mat2), channels, new Mat(), hist2, histSize, ranges);

        // Normalizar histogramas
        Core.normalize(hist1, hist1);
        Core.normalize(hist2, hist2);

        // Comparar histogramas
        return Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_CORREL);
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }

        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        mat.get(0, 0, data);
        return image;
    }
}