//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.ml;

import org.opencv.core.Mat;

// C++: class DTrees

/**
 * The class represents a single decision tree or a collection of decision trees.
 * <p>
 * The current public interface of the class allows user to train only a single decision tree, however
 * the class is capable of storing multiple decision trees and using them for prediction (by summing
 * responses or using a voting schemes), and the derived from DTrees classes (such as RTrees and Boost)
 * use this capability to implement decision tree ensembles.
 * <p>
 * SEE: REF: ml_intro_trees
 */
public class DTrees extends StatModel {

    // C++: enum Flags (cv.ml.DTrees.Flags)
    public static final int
            PREDICT_AUTO = 0,
            PREDICT_SUM = (1 << 8),
            PREDICT_MAX_VOTE = (2 << 8),
            PREDICT_MASK = (3 << 8);

    protected DTrees(long addr) {
        super(addr);
    }

    // internal usage only
    public static DTrees __fromPtr__(long addr) {
        return new DTrees(addr);
    }


    //
    // C++:  int cv::ml::DTrees::getMaxCategories()
    //

    /**
     * Creates the empty model
     * <p>
     * The static method creates empty decision tree with the specified parameters. It should be then
     * trained using train method (see StatModel::train). Alternatively, you can load the model from
     * file using Algorithm::load&lt;DTrees&gt;(filename).
     *
     * @return automatically generated
     */
    public static DTrees create() {
        return DTrees.__fromPtr__(create_0());
    }


    //
    // C++:  void cv::ml::DTrees::setMaxCategories(int val)
    //

    /**
     * Loads and creates a serialized DTrees from a file
     * <p>
     * Use DTree::save to serialize and store an DTree to disk.
     * Load the DTree from this file again, by calling this function with the path to the file.
     * Optionally specify the node for the file containing the classifier
     *
     * @param filepath path to serialized DTree
     * @param nodeName name of node containing the classifier
     * @return automatically generated
     */
    public static DTrees load(String filepath, String nodeName) {
        return DTrees.__fromPtr__(load_0(filepath, nodeName));
    }


    //
    // C++:  int cv::ml::DTrees::getMaxDepth()
    //

    /**
     * Loads and creates a serialized DTrees from a file
     * <p>
     * Use DTree::save to serialize and store an DTree to disk.
     * Load the DTree from this file again, by calling this function with the path to the file.
     * Optionally specify the node for the file containing the classifier
     *
     * @param filepath path to serialized DTree
     * @return automatically generated
     */
    public static DTrees load(String filepath) {
        return DTrees.__fromPtr__(load_1(filepath));
    }


    //
    // C++:  void cv::ml::DTrees::setMaxDepth(int val)
    //

    // C++:  int cv::ml::DTrees::getMaxCategories()
    private static native int getMaxCategories_0(long nativeObj);


    //
    // C++:  int cv::ml::DTrees::getMinSampleCount()
    //

    // C++:  void cv::ml::DTrees::setMaxCategories(int val)
    private static native void setMaxCategories_0(long nativeObj, int val);


    //
    // C++:  void cv::ml::DTrees::setMinSampleCount(int val)
    //

    // C++:  int cv::ml::DTrees::getMaxDepth()
    private static native int getMaxDepth_0(long nativeObj);


    //
    // C++:  int cv::ml::DTrees::getCVFolds()
    //

    // C++:  void cv::ml::DTrees::setMaxDepth(int val)
    private static native void setMaxDepth_0(long nativeObj, int val);


    //
    // C++:  void cv::ml::DTrees::setCVFolds(int val)
    //

    // C++:  int cv::ml::DTrees::getMinSampleCount()
    private static native int getMinSampleCount_0(long nativeObj);


    //
    // C++:  bool cv::ml::DTrees::getUseSurrogates()
    //

    // C++:  void cv::ml::DTrees::setMinSampleCount(int val)
    private static native void setMinSampleCount_0(long nativeObj, int val);


    //
    // C++:  void cv::ml::DTrees::setUseSurrogates(bool val)
    //

    // C++:  int cv::ml::DTrees::getCVFolds()
    private static native int getCVFolds_0(long nativeObj);


    //
    // C++:  bool cv::ml::DTrees::getUse1SERule()
    //

    // C++:  void cv::ml::DTrees::setCVFolds(int val)
    private static native void setCVFolds_0(long nativeObj, int val);


    //
    // C++:  void cv::ml::DTrees::setUse1SERule(bool val)
    //

    // C++:  bool cv::ml::DTrees::getUseSurrogates()
    private static native boolean getUseSurrogates_0(long nativeObj);


    //
    // C++:  bool cv::ml::DTrees::getTruncatePrunedTree()
    //

    // C++:  void cv::ml::DTrees::setUseSurrogates(bool val)
    private static native void setUseSurrogates_0(long nativeObj, boolean val);


    //
    // C++:  void cv::ml::DTrees::setTruncatePrunedTree(bool val)
    //

    // C++:  bool cv::ml::DTrees::getUse1SERule()
    private static native boolean getUse1SERule_0(long nativeObj);


    //
    // C++:  float cv::ml::DTrees::getRegressionAccuracy()
    //

    // C++:  void cv::ml::DTrees::setUse1SERule(bool val)
    private static native void setUse1SERule_0(long nativeObj, boolean val);


    //
    // C++:  void cv::ml::DTrees::setRegressionAccuracy(float val)
    //

    // C++:  bool cv::ml::DTrees::getTruncatePrunedTree()
    private static native boolean getTruncatePrunedTree_0(long nativeObj);


    //
    // C++:  Mat cv::ml::DTrees::getPriors()
    //

    // C++:  void cv::ml::DTrees::setTruncatePrunedTree(bool val)
    private static native void setTruncatePrunedTree_0(long nativeObj, boolean val);


    //
    // C++:  void cv::ml::DTrees::setPriors(Mat val)
    //

    // C++:  float cv::ml::DTrees::getRegressionAccuracy()
    private static native float getRegressionAccuracy_0(long nativeObj);


    //
    // C++: static Ptr_DTrees cv::ml::DTrees::create()
    //

    // C++:  void cv::ml::DTrees::setRegressionAccuracy(float val)
    private static native void setRegressionAccuracy_0(long nativeObj, float val);


    //
    // C++: static Ptr_DTrees cv::ml::DTrees::load(String filepath, String nodeName = String())
    //

    // C++:  Mat cv::ml::DTrees::getPriors()
    private static native long getPriors_0(long nativeObj);

    // C++:  void cv::ml::DTrees::setPriors(Mat val)
    private static native void setPriors_0(long nativeObj, long val_nativeObj);

    // C++: static Ptr_DTrees cv::ml::DTrees::create()
    private static native long create_0();

    // C++: static Ptr_DTrees cv::ml::DTrees::load(String filepath, String nodeName = String())
    private static native long load_0(String filepath, String nodeName);

    private static native long load_1(String filepath);

    // native support for java finalize()
    private static native void delete(long nativeObj);

    /**
     * SEE: setMaxCategories
     *
     * @return automatically generated
     */
    public int getMaxCategories() {
        return getMaxCategories_0(nativeObj);
    }

    /**
     * getMaxCategories SEE: getMaxCategories
     *
     * @param val automatically generated
     */
    public void setMaxCategories(int val) {
        setMaxCategories_0(nativeObj, val);
    }

    /**
     * SEE: setMaxDepth
     *
     * @return automatically generated
     */
    public int getMaxDepth() {
        return getMaxDepth_0(nativeObj);
    }

    /**
     * getMaxDepth SEE: getMaxDepth
     *
     * @param val automatically generated
     */
    public void setMaxDepth(int val) {
        setMaxDepth_0(nativeObj, val);
    }

    /**
     * SEE: setMinSampleCount
     *
     * @return automatically generated
     */
    public int getMinSampleCount() {
        return getMinSampleCount_0(nativeObj);
    }

    /**
     * getMinSampleCount SEE: getMinSampleCount
     *
     * @param val automatically generated
     */
    public void setMinSampleCount(int val) {
        setMinSampleCount_0(nativeObj, val);
    }

    /**
     * SEE: setCVFolds
     *
     * @return automatically generated
     */
    public int getCVFolds() {
        return getCVFolds_0(nativeObj);
    }

    /**
     * getCVFolds SEE: getCVFolds
     *
     * @param val automatically generated
     */
    public void setCVFolds(int val) {
        setCVFolds_0(nativeObj, val);
    }

    /**
     * SEE: setUseSurrogates
     *
     * @return automatically generated
     */
    public boolean getUseSurrogates() {
        return getUseSurrogates_0(nativeObj);
    }

    /**
     * getUseSurrogates SEE: getUseSurrogates
     *
     * @param val automatically generated
     */
    public void setUseSurrogates(boolean val) {
        setUseSurrogates_0(nativeObj, val);
    }

    /**
     * SEE: setUse1SERule
     *
     * @return automatically generated
     */
    public boolean getUse1SERule() {
        return getUse1SERule_0(nativeObj);
    }

    /**
     * getUse1SERule SEE: getUse1SERule
     *
     * @param val automatically generated
     */
    public void setUse1SERule(boolean val) {
        setUse1SERule_0(nativeObj, val);
    }

    /**
     * SEE: setTruncatePrunedTree
     *
     * @return automatically generated
     */
    public boolean getTruncatePrunedTree() {
        return getTruncatePrunedTree_0(nativeObj);
    }

    /**
     * getTruncatePrunedTree SEE: getTruncatePrunedTree
     *
     * @param val automatically generated
     */
    public void setTruncatePrunedTree(boolean val) {
        setTruncatePrunedTree_0(nativeObj, val);
    }

    /**
     * SEE: setRegressionAccuracy
     *
     * @return automatically generated
     */
    public float getRegressionAccuracy() {
        return getRegressionAccuracy_0(nativeObj);
    }

    /**
     * getRegressionAccuracy SEE: getRegressionAccuracy
     *
     * @param val automatically generated
     */
    public void setRegressionAccuracy(float val) {
        setRegressionAccuracy_0(nativeObj, val);
    }

    /**
     * SEE: setPriors
     *
     * @return automatically generated
     */
    public Mat getPriors() {
        return new Mat(getPriors_0(nativeObj));
    }

    /**
     * getPriors SEE: getPriors
     *
     * @param val automatically generated
     */
    public void setPriors(Mat val) {
        setPriors_0(nativeObj, val.nativeObj);
    }

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }

}
