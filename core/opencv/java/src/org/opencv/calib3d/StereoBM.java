//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.calib3d;

import org.opencv.calib3d.StereoBM;
import org.opencv.calib3d.StereoMatcher;
import org.opencv.core.Rect;

// C++: class StereoBM
/**
 * Class for computing stereo correspondence using the block matching algorithm, introduced and contributed to OpenCV by K. Konolige.
 * This class implements a block matching algorithm for stereo correspondence, which is used to compute disparity maps from stereo image pairs. It provides methods to fine-tune parameters such as pre-filtering, texture thresholds, uniqueness ratios, and regions of interest (ROIs) to optimize performance and accuracy.
 */
public class StereoBM extends StereoMatcher {

    protected StereoBM(long addr) { super(addr); }

    // internal usage only
    public static StereoBM __fromPtr__(long addr) { return new StereoBM(addr); }

    // C++: enum <unnamed>
    public static final int
            PREFILTER_NORMALIZED_RESPONSE = 0,
            PREFILTER_XSOBEL = 1;


    //
    // C++:  int cv::StereoBM::getPreFilterType()
    //

    /**
     * Gets the type of pre-filtering currently used in the algorithm.
     * @return The current pre-filter type: 0 for PREFILTER_NORMALIZED_RESPONSE or 1 for PREFILTER_XSOBEL.
     */
    public int getPreFilterType() {
        return getPreFilterType_0(nativeObj);
    }


    //
    // C++:  void cv::StereoBM::setPreFilterType(int preFilterType)
    //

    /**
     * Sets the type of pre-filtering used in the algorithm.
     * @param preFilterType The type of pre-filter to use. Possible values are:
     * - PREFILTER_NORMALIZED_RESPONSE (0): Uses normalized response for pre-filtering.
     * - PREFILTER_XSOBEL (1): Uses the X-Sobel operator for pre-filtering.
     * The pre-filter type affects how the images are prepared before computing the disparity map. Different pre-filtering methods can enhance specific image features or reduce noise, influencing the quality of the disparity map.
     */
    public void setPreFilterType(int preFilterType) {
        setPreFilterType_0(nativeObj, preFilterType);
    }


    //
    // C++:  int cv::StereoBM::getPreFilterSize()
    //

    /**
     * Gets the current size of the pre-filter kernel.
     * @return The current pre-filter size.
     */
    public int getPreFilterSize() {
        return getPreFilterSize_0(nativeObj);
    }


    //
    // C++:  void cv::StereoBM::setPreFilterSize(int preFilterSize)
    //

    /**
     * Sets the size of the pre-filter kernel.
     * @param preFilterSize The size of the pre-filter kernel. Must be an odd integer, typically between 5 and 255.
     * The pre-filter size determines the spatial extent of the pre-filtering operation, which prepares the images for disparity computation by normalizing brightness and enhancing texture. Larger sizes reduce noise but may blur details, while smaller sizes preserve details but are more susceptible to noise.
     */
    public void setPreFilterSize(int preFilterSize) {
        setPreFilterSize_0(nativeObj, preFilterSize);
    }


    //
    // C++:  int cv::StereoBM::getPreFilterCap()
    //

    /**
     * Gets the current truncation value for prefiltered pixels.
     * @return The current pre-filter cap value.
     */
    public int getPreFilterCap() {
        return getPreFilterCap_0(nativeObj);
    }


    //
    // C++:  void cv::StereoBM::setPreFilterCap(int preFilterCap)
    //

    /**
     * Sets the truncation value for prefiltered pixels.
     * @param preFilterCap The truncation value. Typically in the range [1, 63].
     * This value caps the output of the pre-filter to [-preFilterCap, preFilterCap], helping to reduce the impact of noise and outliers in the pre-filtered image.
     */
    public void setPreFilterCap(int preFilterCap) {
        setPreFilterCap_0(nativeObj, preFilterCap);
    }


    //
    // C++:  int cv::StereoBM::getTextureThreshold()
    //

    /**
     * Gets the current texture threshold value.
     * @return The current texture threshold.
     */
    public int getTextureThreshold() {
        return getTextureThreshold_0(nativeObj);
    }


    //
    // C++:  void cv::StereoBM::setTextureThreshold(int textureThreshold)
    //

    /**
     * Sets the threshold for filtering low-texture regions.
     * @param textureThreshold The threshold value. Must be non-negative.
     * This parameter filters out regions with low texture, where establishing correspondences is difficult, thus reducing noise in the disparity map. Higher values filter more aggressively but may discard valid information.
     */
    public void setTextureThreshold(int textureThreshold) {
        setTextureThreshold_0(nativeObj, textureThreshold);
    }


    //
    // C++:  int cv::StereoBM::getUniquenessRatio()
    //

    /**
     * Gets the current uniqueness ratio value.
     * @return The current uniqueness ratio.
     */
    public int getUniquenessRatio() {
        return getUniquenessRatio_0(nativeObj);
    }


    //
    // C++:  void cv::StereoBM::setUniquenessRatio(int uniquenessRatio)
    //

    /**
     * Sets the uniqueness ratio for filtering ambiguous matches.
     * @param uniquenessRatio The uniqueness ratio value. Typically in the range [5, 15], but can be from 0 to 100.
     * This parameter ensures that the best match is sufficiently better than the next best match, reducing false positives. Higher values are stricter but may filter out valid matches in difficult regions.
     */
    public void setUniquenessRatio(int uniquenessRatio) {
        setUniquenessRatio_0(nativeObj, uniquenessRatio);
    }


    //
    // C++:  int cv::StereoBM::getSmallerBlockSize()
    //

    /**
     * Gets the current size of the smaller block used for texture check.
     * @return The current smaller block size.
     */
    public int getSmallerBlockSize() {
        return getSmallerBlockSize_0(nativeObj);
    }


    //
    // C++:  void cv::StereoBM::setSmallerBlockSize(int blockSize)
    //

    /**
     * Sets the size of the smaller block used for texture check.
     * @param blockSize The size of the smaller block. Must be an odd integer between 5 and 255.
     * This parameter determines the size of the block used to compute texture variance. Smaller blocks capture finer details but are more sensitive to noise, while larger blocks are more robust but may miss fine details.
     */
    public void setSmallerBlockSize(int blockSize) {
        setSmallerBlockSize_0(nativeObj, blockSize);
    }


    //
    // C++:  Rect cv::StereoBM::getROI1()
    //

    /**
     * Gets the current Region of Interest (ROI) for the left image.
     * @return The current ROI for the left image.
     */
    public Rect getROI1() {
        return new Rect(getROI1_0(nativeObj));
    }


    //
    // C++:  void cv::StereoBM::setROI1(Rect roi1)
    //

    /**
     * Sets the Region of Interest (ROI) for the left image.
     * @param roi1 The ROI rectangle for the left image.
     * By setting the ROI, the stereo matching computation is limited to the specified region, improving performance and potentially accuracy by focusing on relevant parts of the image.
     */
    public void setROI1(Rect roi1) {
        setROI1_0(nativeObj, roi1.x, roi1.y, roi1.width, roi1.height);
    }


    //
    // C++:  Rect cv::StereoBM::getROI2()
    //

    /**
     * Gets the current Region of Interest (ROI) for the right image.
     * @return The current ROI for the right image.
     */
    public Rect getROI2() {
        return new Rect(getROI2_0(nativeObj));
    }


    //
    // C++:  void cv::StereoBM::setROI2(Rect roi2)
    //

    /**
     * Sets the Region of Interest (ROI) for the right image.
     * @param roi2 The ROI rectangle for the right image.
     * Similar to setROI1, this limits the computation to the specified region in the right image.
     */
    public void setROI2(Rect roi2) {
        setROI2_0(nativeObj, roi2.x, roi2.y, roi2.width, roi2.height);
    }


    //
    // C++: static Ptr_StereoBM cv::StereoBM::create(int numDisparities = 0, int blockSize = 21)
    //

    /**
     * Creates StereoBM object
     * @param numDisparities The disparity search range. For each pixel, the algorithm will find the best disparity from 0 (default minimum disparity) to numDisparities. The search range can be shifted by changing the minimum disparity.
     * @param blockSize The linear size of the blocks compared by the algorithm. The size should be odd (as the block is centered at the current pixel). Larger block size implies smoother, though less accurate disparity map. Smaller block size gives more detailed disparity map, but there is a higher chance for the algorithm to find a wrong correspondence.
     * @return A pointer to the created StereoBM object.
     * The function creates a StereoBM object. You can then call StereoBM::compute() to compute disparity for a specific stereo pair.
     */
    public static StereoBM create(int numDisparities, int blockSize) {
        return StereoBM.__fromPtr__(create_0(numDisparities, blockSize));
    }

    /**
     * Creates StereoBM object
     * @param numDisparities The disparity search range. For each pixel, the algorithm will find the best disparity from 0 (default minimum disparity) to numDisparities. The search range can be shifted by changing the minimum disparity.
     * @return A pointer to the created StereoBM object.
     * The function creates a StereoBM object. You can then call StereoBM::compute() to compute disparity for a specific stereo pair.
     */
    public static StereoBM create(int numDisparities) {
        return StereoBM.__fromPtr__(create_1(numDisparities));
    }

    /**
     * Creates StereoBM object
     * @return A pointer to the created StereoBM object.
     * The function creates a StereoBM object. You can then call StereoBM::compute() to compute disparity for a specific stereo pair.
     */
    public static StereoBM create() {
        return StereoBM.__fromPtr__(create_2());
    }


    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }



    // C++:  int cv::StereoBM::getPreFilterType()
    private static native int getPreFilterType_0(long nativeObj);

    // C++:  void cv::StereoBM::setPreFilterType(int preFilterType)
    private static native void setPreFilterType_0(long nativeObj, int preFilterType);

    // C++:  int cv::StereoBM::getPreFilterSize()
    private static native int getPreFilterSize_0(long nativeObj);

    // C++:  void cv::StereoBM::setPreFilterSize(int preFilterSize)
    private static native void setPreFilterSize_0(long nativeObj, int preFilterSize);

    // C++:  int cv::StereoBM::getPreFilterCap()
    private static native int getPreFilterCap_0(long nativeObj);

    // C++:  void cv::StereoBM::setPreFilterCap(int preFilterCap)
    private static native void setPreFilterCap_0(long nativeObj, int preFilterCap);

    // C++:  int cv::StereoBM::getTextureThreshold()
    private static native int getTextureThreshold_0(long nativeObj);

    // C++:  void cv::StereoBM::setTextureThreshold(int textureThreshold)
    private static native void setTextureThreshold_0(long nativeObj, int textureThreshold);

    // C++:  int cv::StereoBM::getUniquenessRatio()
    private static native int getUniquenessRatio_0(long nativeObj);

    // C++:  void cv::StereoBM::setUniquenessRatio(int uniquenessRatio)
    private static native void setUniquenessRatio_0(long nativeObj, int uniquenessRatio);

    // C++:  int cv::StereoBM::getSmallerBlockSize()
    private static native int getSmallerBlockSize_0(long nativeObj);

    // C++:  void cv::StereoBM::setSmallerBlockSize(int blockSize)
    private static native void setSmallerBlockSize_0(long nativeObj, int blockSize);

    // C++:  Rect cv::StereoBM::getROI1()
    private static native double[] getROI1_0(long nativeObj);

    // C++:  void cv::StereoBM::setROI1(Rect roi1)
    private static native void setROI1_0(long nativeObj, int roi1_x, int roi1_y, int roi1_width, int roi1_height);

    // C++:  Rect cv::StereoBM::getROI2()
    private static native double[] getROI2_0(long nativeObj);

    // C++:  void cv::StereoBM::setROI2(Rect roi2)
    private static native void setROI2_0(long nativeObj, int roi2_x, int roi2_y, int roi2_width, int roi2_height);

    // C++: static Ptr_StereoBM cv::StereoBM::create(int numDisparities = 0, int blockSize = 21)
    private static native long create_0(int numDisparities, int blockSize);
    private static native long create_1(int numDisparities);
    private static native long create_2();

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
