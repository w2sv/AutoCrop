//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.calib3d;


// C++: class UsacParams

public class UsacParams {

    protected final long nativeObj;

    protected UsacParams(long addr) {
        nativeObj = addr;
    }

    public UsacParams() {
        nativeObj = UsacParams_0();
    }

    // internal usage only
    public static UsacParams __fromPtr__(long addr) {
        return new UsacParams(addr);
    }

    //
    // C++:   cv::UsacParams::UsacParams()
    //

    // C++:   cv::UsacParams::UsacParams()
    private static native long UsacParams_0();


    //
    // C++: double UsacParams::confidence
    //

    // C++: double UsacParams::confidence
    private static native double get_confidence_0(long nativeObj);


    //
    // C++: void UsacParams::confidence
    //

    // C++: void UsacParams::confidence
    private static native void set_confidence_0(long nativeObj, double confidence);


    //
    // C++: bool UsacParams::isParallel
    //

    // C++: bool UsacParams::isParallel
    private static native boolean get_isParallel_0(long nativeObj);


    //
    // C++: void UsacParams::isParallel
    //

    // C++: void UsacParams::isParallel
    private static native void set_isParallel_0(long nativeObj, boolean isParallel);


    //
    // C++: int UsacParams::loIterations
    //

    // C++: int UsacParams::loIterations
    private static native int get_loIterations_0(long nativeObj);


    //
    // C++: void UsacParams::loIterations
    //

    // C++: void UsacParams::loIterations
    private static native void set_loIterations_0(long nativeObj, int loIterations);


    //
    // C++: LocalOptimMethod UsacParams::loMethod
    //

    // C++: LocalOptimMethod UsacParams::loMethod
    private static native int get_loMethod_0(long nativeObj);


    //
    // C++: void UsacParams::loMethod
    //

    // C++: void UsacParams::loMethod
    private static native void set_loMethod_0(long nativeObj, int loMethod);


    //
    // C++: int UsacParams::loSampleSize
    //

    // C++: int UsacParams::loSampleSize
    private static native int get_loSampleSize_0(long nativeObj);


    //
    // C++: void UsacParams::loSampleSize
    //

    // C++: void UsacParams::loSampleSize
    private static native void set_loSampleSize_0(long nativeObj, int loSampleSize);


    //
    // C++: int UsacParams::maxIterations
    //

    // C++: int UsacParams::maxIterations
    private static native int get_maxIterations_0(long nativeObj);


    //
    // C++: void UsacParams::maxIterations
    //

    // C++: void UsacParams::maxIterations
    private static native void set_maxIterations_0(long nativeObj, int maxIterations);


    //
    // C++: NeighborSearchMethod UsacParams::neighborsSearch
    //

    // C++: NeighborSearchMethod UsacParams::neighborsSearch
    private static native int get_neighborsSearch_0(long nativeObj);


    //
    // C++: void UsacParams::neighborsSearch
    //

    // C++: void UsacParams::neighborsSearch
    private static native void set_neighborsSearch_0(long nativeObj, int neighborsSearch);


    //
    // C++: int UsacParams::randomGeneratorState
    //

    // C++: int UsacParams::randomGeneratorState
    private static native int get_randomGeneratorState_0(long nativeObj);


    //
    // C++: void UsacParams::randomGeneratorState
    //

    // C++: void UsacParams::randomGeneratorState
    private static native void set_randomGeneratorState_0(long nativeObj, int randomGeneratorState);


    //
    // C++: SamplingMethod UsacParams::sampler
    //

    // C++: SamplingMethod UsacParams::sampler
    private static native int get_sampler_0(long nativeObj);


    //
    // C++: void UsacParams::sampler
    //

    // C++: void UsacParams::sampler
    private static native void set_sampler_0(long nativeObj, int sampler);


    //
    // C++: ScoreMethod UsacParams::score
    //

    // C++: ScoreMethod UsacParams::score
    private static native int get_score_0(long nativeObj);


    //
    // C++: void UsacParams::score
    //

    // C++: void UsacParams::score
    private static native void set_score_0(long nativeObj, int score);


    //
    // C++: double UsacParams::threshold
    //

    // C++: double UsacParams::threshold
    private static native double get_threshold_0(long nativeObj);


    //
    // C++: void UsacParams::threshold
    //

    // C++: void UsacParams::threshold
    private static native void set_threshold_0(long nativeObj, double threshold);

    // native support for java finalize()
    private static native void delete(long nativeObj);

    public long getNativeObjAddr() {
        return nativeObj;
    }

    public double get_confidence() {
        return get_confidence_0(nativeObj);
    }

    public void set_confidence(double confidence) {
        set_confidence_0(nativeObj, confidence);
    }

    public boolean get_isParallel() {
        return get_isParallel_0(nativeObj);
    }

    public void set_isParallel(boolean isParallel) {
        set_isParallel_0(nativeObj, isParallel);
    }

    public int get_loIterations() {
        return get_loIterations_0(nativeObj);
    }

    public void set_loIterations(int loIterations) {
        set_loIterations_0(nativeObj, loIterations);
    }

    public int get_loMethod() {
        return get_loMethod_0(nativeObj);
    }

    public void set_loMethod(int loMethod) {
        set_loMethod_0(nativeObj, loMethod);
    }

    public int get_loSampleSize() {
        return get_loSampleSize_0(nativeObj);
    }

    public void set_loSampleSize(int loSampleSize) {
        set_loSampleSize_0(nativeObj, loSampleSize);
    }

    public int get_maxIterations() {
        return get_maxIterations_0(nativeObj);
    }

    public void set_maxIterations(int maxIterations) {
        set_maxIterations_0(nativeObj, maxIterations);
    }

    public int get_neighborsSearch() {
        return get_neighborsSearch_0(nativeObj);
    }

    public void set_neighborsSearch(int neighborsSearch) {
        set_neighborsSearch_0(nativeObj, neighborsSearch);
    }

    public int get_randomGeneratorState() {
        return get_randomGeneratorState_0(nativeObj);
    }

    public void set_randomGeneratorState(int randomGeneratorState) {
        set_randomGeneratorState_0(nativeObj, randomGeneratorState);
    }

    public int get_sampler() {
        return get_sampler_0(nativeObj);
    }

    public void set_sampler(int sampler) {
        set_sampler_0(nativeObj, sampler);
    }

    public int get_score() {
        return get_score_0(nativeObj);
    }

    public void set_score(int score) {
        set_score_0(nativeObj, score);
    }

    public double get_threshold() {
        return get_threshold_0(nativeObj);
    }

    public void set_threshold(double threshold) {
        set_threshold_0(nativeObj, threshold);
    }

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }

}
