package weka.dl4j.interpretability;

import weka.classifiers.functions.dl4j.Utils;
import weka.core.progress.ProgressManager;

import javax.imageio.ImageIO;
import java.io.File;

// TODO Document
public class WekaScoreCAM extends AbstractCNNSaliencyMapWrapper {

    /**
     * Displays progress of the current process (feature extraction, training, etc.)
     */
    protected ProgressManager progressManager;

    protected ScoreCAM scoreCAM;

    @Override
    public void processImage(File imageFile) {
        scoreCAM = new ScoreCAM();
        scoreCAM.setComputationGraph(getComputationGraph());
        scoreCAM.setBatchSize(batchSize);

        scoreCAM.setImageChannelsLast(zooModel.getChannelsLast()); // TODO check with non-zooModels
        scoreCAM.setModelInputShape(Utils.decodeCNNShape(zooModel.getShape()[0]));
        scoreCAM.setImagePreProcessingScaler(zooModel.getImagePreprocessingScaler());

        scoreCAM.addIterationsStartedListener(this::onIterationsStarted);
        scoreCAM.addIterationIncrementListener(this::onIterationIncremented);
        scoreCAM.addIterationsFinishedListeners(this::onIterationsFinished);

        scoreCAM.processMaskedImages(imageFile);
    }

    @Override
    public void generateOutputMap() {
        scoreCAM.setTargetClassID(targetClassID);
        scoreCAM.generateOutputMap();
        saveResult();
    }

    private void saveResult() {
        if (Utils.notDefaultFileLocation(getOutputFile())) {
            System.out.println(String.format("Output file location = %s", getOutputFile()));
            try {
                ImageIO.write(scoreCAM.getCompositeImage(), "png", getOutputFile());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            System.err.println("No output file location given - not saving saliency map");
        }
    }

    private void onIterationsStarted(int maxIterations) {
        progressManager = new ProgressManager(maxIterations, "Calculating Saliency Map...");
        progressManager.start();
    }

    private void onIterationIncremented() {
        progressManager.increment();
    }

    private void onIterationsFinished() {
        progressManager.finish();
    }

}