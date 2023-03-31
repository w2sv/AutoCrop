//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.objdetect;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Size;

// C++: class CharucoBoard

/**
 * ChArUco board is a planar chessboard where the markers are placed inside the white squares of a chessboard.
 * <p>
 * The benefits of ChArUco boards is that they provide both, ArUco markers versatility and chessboard corner precision,
 * which is important for calibration and pose estimation. The board image can be drawn using generateImage() method.
 */
public class CharucoBoard extends Board {

    protected CharucoBoard(long addr) {
        super(addr);
    }

    /**
     * CharucoBoard constructor
     *
     * @param size         number of chessboard squares in x and y directions
     * @param squareLength squareLength chessboard square side length (normally in meters)
     * @param markerLength marker side length (same unit than squareLength)
     * @param dictionary   dictionary of markers indicating the type of markers
     * @param ids          array of id used markers
     *                     The first markers in the dictionary are used to fill the white chessboard squares.
     */
    public CharucoBoard(Size size, float squareLength, float markerLength, Dictionary dictionary, Mat ids) {
        super(CharucoBoard_0(size.width, size.height, squareLength, markerLength, dictionary.nativeObj, ids.nativeObj));
    }

    //
    // C++:   cv::aruco::CharucoBoard::CharucoBoard(Size size, float squareLength, float markerLength, Dictionary dictionary, Mat ids = Mat())
    //

    /**
     * CharucoBoard constructor
     *
     * @param size         number of chessboard squares in x and y directions
     * @param squareLength squareLength chessboard square side length (normally in meters)
     * @param markerLength marker side length (same unit than squareLength)
     * @param dictionary   dictionary of markers indicating the type of markers
     *                     The first markers in the dictionary are used to fill the white chessboard squares.
     */
    public CharucoBoard(Size size, float squareLength, float markerLength, Dictionary dictionary) {
        super(CharucoBoard_1(size.width, size.height, squareLength, markerLength, dictionary.nativeObj));
    }

    // internal usage only
    public static CharucoBoard __fromPtr__(long addr) {
        return new CharucoBoard(addr);
    }


    //
    // C++:  Size cv::aruco::CharucoBoard::getChessboardSize()
    //

    // C++:   cv::aruco::CharucoBoard::CharucoBoard(Size size, float squareLength, float markerLength, Dictionary dictionary, Mat ids = Mat())
    private static native long CharucoBoard_0(double size_width, double size_height, float squareLength, float markerLength, long dictionary_nativeObj, long ids_nativeObj);


    //
    // C++:  float cv::aruco::CharucoBoard::getSquareLength()
    //

    private static native long CharucoBoard_1(double size_width, double size_height, float squareLength, float markerLength, long dictionary_nativeObj);


    //
    // C++:  float cv::aruco::CharucoBoard::getMarkerLength()
    //

    // C++:  Size cv::aruco::CharucoBoard::getChessboardSize()
    private static native double[] getChessboardSize_0(long nativeObj);


    //
    // C++:  vector_Point3f cv::aruco::CharucoBoard::getChessboardCorners()
    //

    // C++:  float cv::aruco::CharucoBoard::getSquareLength()
    private static native float getSquareLength_0(long nativeObj);


    //
    // C++:  bool cv::aruco::CharucoBoard::checkCharucoCornersCollinear(Mat charucoIds)
    //

    // C++:  float cv::aruco::CharucoBoard::getMarkerLength()
    private static native float getMarkerLength_0(long nativeObj);

    // C++:  vector_Point3f cv::aruco::CharucoBoard::getChessboardCorners()
    private static native long getChessboardCorners_0(long nativeObj);

    // C++:  bool cv::aruco::CharucoBoard::checkCharucoCornersCollinear(Mat charucoIds)
    private static native boolean checkCharucoCornersCollinear_0(long nativeObj, long charucoIds_nativeObj);

    // native support for java finalize()
    private static native void delete(long nativeObj);

    public Size getChessboardSize() {
        return new Size(getChessboardSize_0(nativeObj));
    }

    public float getSquareLength() {
        return getSquareLength_0(nativeObj);
    }

    public float getMarkerLength() {
        return getMarkerLength_0(nativeObj);
    }

    /**
     * get CharucoBoard::chessboardCorners
     *
     * @return automatically generated
     */
    public MatOfPoint3f getChessboardCorners() {
        return MatOfPoint3f.fromNativeAddr(getChessboardCorners_0(nativeObj));
    }

    /**
     * check whether the ChArUco markers are collinear
     *
     * @param charucoIds list of identifiers for each corner in charucoCorners per frame.
     * @return bool value, 1 (true) if detected corners form a line, 0 (false) if they do not.
     * solvePnP, calibration functions will fail if the corners are collinear (true).
     * <p>
     * The number of ids in charucoIDs should be &lt;= the number of chessboard corners in the board.
     * This functions checks whether the charuco corners are on a straight line (returns true, if so), or not (false).
     * Axis parallel, as well as diagonal and other straight lines detected.  Degenerate cases:
     * for number of charucoIDs &lt;= 2,the function returns true.
     */
    public boolean checkCharucoCornersCollinear(Mat charucoIds) {
        return checkCharucoCornersCollinear_0(nativeObj, charucoIds.nativeObj);
    }

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }

}
