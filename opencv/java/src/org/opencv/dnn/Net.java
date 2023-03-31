//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.dnn;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Scalar;
import org.opencv.utils.Converters;

import java.util.List;

// C++: class Net

/**
 * This class allows to create and manipulate comprehensive artificial neural networks.
 * <p>
 * Neural network is presented as directed acyclic graph (DAG), where vertices are Layer instances,
 * and edges specify relationships between layers inputs and outputs.
 * <p>
 * Each network layer has unique integer id and unique string name inside its network.
 * LayerId can store either layer name or layer id.
 * <p>
 * This class supports reference counting of its instances, i. e. copies point to the same instance.
 */
public class Net {

    protected final long nativeObj;

    protected Net(long addr) {
        nativeObj = addr;
    }

    public Net() {
        nativeObj = Net_0();
    }

    // internal usage only
    public static Net __fromPtr__(long addr) {
        return new Net(addr);
    }

    //
    // C++:   cv::dnn::Net::Net()
    //

    /**
     * Create a network from Intel's Model Optimizer intermediate representation (IR).
     *
     * @param xml XML configuration file with network's topology.
     * @param bin Binary file with trained weights.
     *            Networks imported from Intel's Model Optimizer are launched in Intel's Inference Engine
     *            backend.
     * @return automatically generated
     */
    public static Net readFromModelOptimizer(String xml, String bin) {
        return new Net(readFromModelOptimizer_0(xml, bin));
    }


    //
    // C++: static Net cv::dnn::Net::readFromModelOptimizer(String xml, String bin)
    //

    /**
     * Create a network from Intel's Model Optimizer in-memory buffers with intermediate representation (IR).
     *
     * @param bufferModelConfig buffer with model's configuration.
     * @param bufferWeights     buffer with model's trained weights.
     * @return Net object.
     */
    public static Net readFromModelOptimizer(MatOfByte bufferModelConfig, MatOfByte bufferWeights) {
        Mat bufferModelConfig_mat = bufferModelConfig;
        Mat bufferWeights_mat = bufferWeights;
        return new Net(readFromModelOptimizer_1(bufferModelConfig_mat.nativeObj, bufferWeights_mat.nativeObj));
    }


    //
    // C++: static Net cv::dnn::Net::readFromModelOptimizer(vector_uchar bufferModelConfig, vector_uchar bufferWeights)
    //

    // C++:   cv::dnn::Net::Net()
    private static native long Net_0();


    //
    // C++:  bool cv::dnn::Net::empty()
    //

    // C++: static Net cv::dnn::Net::readFromModelOptimizer(String xml, String bin)
    private static native long readFromModelOptimizer_0(String xml, String bin);


    //
    // C++:  String cv::dnn::Net::dump()
    //

    // C++: static Net cv::dnn::Net::readFromModelOptimizer(vector_uchar bufferModelConfig, vector_uchar bufferWeights)
    private static native long readFromModelOptimizer_1(long bufferModelConfig_mat_nativeObj, long bufferWeights_mat_nativeObj);


    //
    // C++:  void cv::dnn::Net::dumpToFile(String path)
    //

    // C++:  bool cv::dnn::Net::empty()
    private static native boolean empty_0(long nativeObj);


    //
    // C++:  int cv::dnn::Net::getLayerId(String layer)
    //

    // C++:  String cv::dnn::Net::dump()
    private static native String dump_0(long nativeObj);


    //
    // C++:  vector_String cv::dnn::Net::getLayerNames()
    //

    // C++:  void cv::dnn::Net::dumpToFile(String path)
    private static native void dumpToFile_0(long nativeObj, String path);


    //
    // C++:  Ptr_Layer cv::dnn::Net::getLayer(int layerId)
    //

    // C++:  int cv::dnn::Net::getLayerId(String layer)
    private static native int getLayerId_0(long nativeObj, String layer);


    //
    // C++:  Ptr_Layer cv::dnn::Net::getLayer(String layerName)
    //

    // C++:  vector_String cv::dnn::Net::getLayerNames()
    private static native List<String> getLayerNames_0(long nativeObj);


    //
    // C++:  Ptr_Layer cv::dnn::Net::getLayer(LayerId layerId)
    //

    // C++:  Ptr_Layer cv::dnn::Net::getLayer(int layerId)
    private static native long getLayer_0(long nativeObj, int layerId);


    //
    // C++:  void cv::dnn::Net::connect(String outPin, String inpPin)
    //

    // C++:  Ptr_Layer cv::dnn::Net::getLayer(String layerName)
    private static native long getLayer_1(long nativeObj, String layerName);


    //
    // C++:  void cv::dnn::Net::setInputsNames(vector_String inputBlobNames)
    //

    // C++:  Ptr_Layer cv::dnn::Net::getLayer(LayerId layerId)
    private static native long getLayer_2(long nativeObj, long layerId_nativeObj);


    //
    // C++:  void cv::dnn::Net::setInputShape(String inputName, MatShape shape)
    //

    // C++:  void cv::dnn::Net::connect(String outPin, String inpPin)
    private static native void connect_0(long nativeObj, String outPin, String inpPin);


    //
    // C++:  Mat cv::dnn::Net::forward(String outputName = String())
    //

    // C++:  void cv::dnn::Net::setInputsNames(vector_String inputBlobNames)
    private static native void setInputsNames_0(long nativeObj, List<String> inputBlobNames);

    // C++:  void cv::dnn::Net::setInputShape(String inputName, MatShape shape)
    private static native void setInputShape_0(long nativeObj, String inputName, long shape_mat_nativeObj);


    //
    // C++:  AsyncArray cv::dnn::Net::forwardAsync(String outputName = String())
    //

    // Return type 'AsyncArray' is not supported, skipping the function


    //
    // C++:  void cv::dnn::Net::forward(vector_Mat& outputBlobs, String outputName = String())
    //

    // C++:  Mat cv::dnn::Net::forward(String outputName = String())
    private static native long forward_0(long nativeObj, String outputName);

    private static native long forward_1(long nativeObj);


    //
    // C++:  void cv::dnn::Net::forward(vector_Mat& outputBlobs, vector_String outBlobNames)
    //

    // C++:  void cv::dnn::Net::forward(vector_Mat& outputBlobs, String outputName = String())
    private static native void forward_2(long nativeObj, long outputBlobs_mat_nativeObj, String outputName);


    //
    // C++:  void cv::dnn::Net::forward(vector_vector_Mat& outputBlobs, vector_String outBlobNames)
    //

    // Unknown type 'vector_vector_Mat' (O), skipping the function


    //
    // C++:  Net cv::dnn::Net::quantize(vector_Mat calibData, int inputsDtype, int outputsDtype, bool perChannel = true)
    //

    private static native void forward_3(long nativeObj, long outputBlobs_mat_nativeObj);

    // C++:  void cv::dnn::Net::forward(vector_Mat& outputBlobs, vector_String outBlobNames)
    private static native void forward_4(long nativeObj, long outputBlobs_mat_nativeObj, List<String> outBlobNames);


    //
    // C++:  void cv::dnn::Net::getInputDetails(vector_float& scales, vector_int& zeropoints)
    //

    // C++:  Net cv::dnn::Net::quantize(vector_Mat calibData, int inputsDtype, int outputsDtype, bool perChannel = true)
    private static native long quantize_0(long nativeObj, long calibData_mat_nativeObj, int inputsDtype, int outputsDtype, boolean perChannel);


    //
    // C++:  void cv::dnn::Net::getOutputDetails(vector_float& scales, vector_int& zeropoints)
    //

    private static native long quantize_1(long nativeObj, long calibData_mat_nativeObj, int inputsDtype, int outputsDtype);


    //
    // C++:  void cv::dnn::Net::setHalideScheduler(String scheduler)
    //

    // C++:  void cv::dnn::Net::getInputDetails(vector_float& scales, vector_int& zeropoints)
    private static native void getInputDetails_0(long nativeObj, long scales_mat_nativeObj, long zeropoints_mat_nativeObj);


    //
    // C++:  void cv::dnn::Net::setPreferableBackend(int backendId)
    //

    // C++:  void cv::dnn::Net::getOutputDetails(vector_float& scales, vector_int& zeropoints)
    private static native void getOutputDetails_0(long nativeObj, long scales_mat_nativeObj, long zeropoints_mat_nativeObj);


    //
    // C++:  void cv::dnn::Net::setPreferableTarget(int targetId)
    //

    // C++:  void cv::dnn::Net::setHalideScheduler(String scheduler)
    private static native void setHalideScheduler_0(long nativeObj, String scheduler);


    //
    // C++:  void cv::dnn::Net::setInput(Mat blob, String name = "", double scalefactor = 1.0, Scalar mean = Scalar())
    //

    // C++:  void cv::dnn::Net::setPreferableBackend(int backendId)
    private static native void setPreferableBackend_0(long nativeObj, int backendId);

    // C++:  void cv::dnn::Net::setPreferableTarget(int targetId)
    private static native void setPreferableTarget_0(long nativeObj, int targetId);

    // C++:  void cv::dnn::Net::setInput(Mat blob, String name = "", double scalefactor = 1.0, Scalar mean = Scalar())
    private static native void setInput_0(long nativeObj, long blob_nativeObj, String name, double scalefactor, double mean_val0, double mean_val1, double mean_val2, double mean_val3);

    private static native void setInput_1(long nativeObj, long blob_nativeObj, String name, double scalefactor);


    //
    // C++:  void cv::dnn::Net::setParam(int layer, int numParam, Mat blob)
    //

    private static native void setInput_2(long nativeObj, long blob_nativeObj, String name);


    //
    // C++:  void cv::dnn::Net::setParam(String layerName, int numParam, Mat blob)
    //

    private static native void setInput_3(long nativeObj, long blob_nativeObj);


    //
    // C++:  Mat cv::dnn::Net::getParam(int layer, int numParam = 0)
    //

    // C++:  void cv::dnn::Net::setParam(int layer, int numParam, Mat blob)
    private static native void setParam_0(long nativeObj, int layer, int numParam, long blob_nativeObj);

    // C++:  void cv::dnn::Net::setParam(String layerName, int numParam, Mat blob)
    private static native void setParam_1(long nativeObj, String layerName, int numParam, long blob_nativeObj);


    //
    // C++:  Mat cv::dnn::Net::getParam(String layerName, int numParam = 0)
    //

    // C++:  Mat cv::dnn::Net::getParam(int layer, int numParam = 0)
    private static native long getParam_0(long nativeObj, int layer, int numParam);

    private static native long getParam_1(long nativeObj, int layer);


    //
    // C++:  vector_int cv::dnn::Net::getUnconnectedOutLayers()
    //

    // C++:  Mat cv::dnn::Net::getParam(String layerName, int numParam = 0)
    private static native long getParam_2(long nativeObj, String layerName, int numParam);


    //
    // C++:  vector_String cv::dnn::Net::getUnconnectedOutLayersNames()
    //

    private static native long getParam_3(long nativeObj, String layerName);


    //
    // C++:  void cv::dnn::Net::getLayersShapes(vector_MatShape netInputShapes, vector_int& layersIds, vector_vector_MatShape& inLayersShapes, vector_vector_MatShape& outLayersShapes)
    //

    // Unknown type 'vector_vector_MatShape' (O), skipping the function


    //
    // C++:  void cv::dnn::Net::getLayersShapes(MatShape netInputShape, vector_int& layersIds, vector_vector_MatShape& inLayersShapes, vector_vector_MatShape& outLayersShapes)
    //

    // Unknown type 'vector_vector_MatShape' (O), skipping the function


    //
    // C++:  int64 cv::dnn::Net::getFLOPS(vector_MatShape netInputShapes)
    //

    // C++:  vector_int cv::dnn::Net::getUnconnectedOutLayers()
    private static native long getUnconnectedOutLayers_0(long nativeObj);


    //
    // C++:  int64 cv::dnn::Net::getFLOPS(MatShape netInputShape)
    //

    // C++:  vector_String cv::dnn::Net::getUnconnectedOutLayersNames()
    private static native List<String> getUnconnectedOutLayersNames_0(long nativeObj);


    //
    // C++:  int64 cv::dnn::Net::getFLOPS(int layerId, vector_MatShape netInputShapes)
    //

    // C++:  int64 cv::dnn::Net::getFLOPS(vector_MatShape netInputShapes)
    private static native long getFLOPS_0(long nativeObj, List<MatOfInt> netInputShapes);


    //
    // C++:  int64 cv::dnn::Net::getFLOPS(int layerId, MatShape netInputShape)
    //

    // C++:  int64 cv::dnn::Net::getFLOPS(MatShape netInputShape)
    private static native long getFLOPS_1(long nativeObj, long netInputShape_mat_nativeObj);


    //
    // C++:  void cv::dnn::Net::getLayerTypes(vector_String& layersTypes)
    //

    // C++:  int64 cv::dnn::Net::getFLOPS(int layerId, vector_MatShape netInputShapes)
    private static native long getFLOPS_2(long nativeObj, int layerId, List<MatOfInt> netInputShapes);


    //
    // C++:  int cv::dnn::Net::getLayersCount(String layerType)
    //

    // C++:  int64 cv::dnn::Net::getFLOPS(int layerId, MatShape netInputShape)
    private static native long getFLOPS_3(long nativeObj, int layerId, long netInputShape_mat_nativeObj);


    //
    // C++:  void cv::dnn::Net::getMemoryConsumption(MatShape netInputShape, size_t& weights, size_t& blobs)
    //

    // C++:  void cv::dnn::Net::getLayerTypes(vector_String& layersTypes)
    private static native void getLayerTypes_0(long nativeObj, List<String> layersTypes);


    //
    // C++:  void cv::dnn::Net::getMemoryConsumption(int layerId, vector_MatShape netInputShapes, size_t& weights, size_t& blobs)
    //

    // C++:  int cv::dnn::Net::getLayersCount(String layerType)
    private static native int getLayersCount_0(long nativeObj, String layerType);


    //
    // C++:  void cv::dnn::Net::getMemoryConsumption(int layerId, MatShape netInputShape, size_t& weights, size_t& blobs)
    //

    // C++:  void cv::dnn::Net::getMemoryConsumption(MatShape netInputShape, size_t& weights, size_t& blobs)
    private static native void getMemoryConsumption_0(long nativeObj, long netInputShape_mat_nativeObj, double[] weights_out, double[] blobs_out);


    //
    // C++:  void cv::dnn::Net::enableFusion(bool fusion)
    //

    // C++:  void cv::dnn::Net::getMemoryConsumption(int layerId, vector_MatShape netInputShapes, size_t& weights, size_t& blobs)
    private static native void getMemoryConsumption_1(long nativeObj, int layerId, List<MatOfInt> netInputShapes, double[] weights_out, double[] blobs_out);


    //
    // C++:  void cv::dnn::Net::enableWinograd(bool useWinograd)
    //

    // C++:  void cv::dnn::Net::getMemoryConsumption(int layerId, MatShape netInputShape, size_t& weights, size_t& blobs)
    private static native void getMemoryConsumption_2(long nativeObj, int layerId, long netInputShape_mat_nativeObj, double[] weights_out, double[] blobs_out);


    //
    // C++:  int64 cv::dnn::Net::getPerfProfile(vector_double& timings)
    //

    // C++:  void cv::dnn::Net::enableFusion(bool fusion)
    private static native void enableFusion_0(long nativeObj, boolean fusion);

    // C++:  void cv::dnn::Net::enableWinograd(bool useWinograd)
    private static native void enableWinograd_0(long nativeObj, boolean useWinograd);

    // C++:  int64 cv::dnn::Net::getPerfProfile(vector_double& timings)
    private static native long getPerfProfile_0(long nativeObj, long timings_mat_nativeObj);

    // native support for java finalize()
    private static native void delete(long nativeObj);

    public long getNativeObjAddr() {
        return nativeObj;
    }

    /**
     * Returns true if there are no layers in the network.
     *
     * @return automatically generated
     */
    public boolean empty() {
        return empty_0(nativeObj);
    }

    /**
     * Dump net to String
     *
     * @return String with structure, hyperparameters, backend, target and fusion
     * Call method after setInput(). To see correct backend, target and fusion run after forward().
     */
    public String dump() {
        return dump_0(nativeObj);
    }

    /**
     * Dump net structure, hyperparameters, backend, target and fusion to dot file
     *
     * @param path path to output file with .dot extension
     *             SEE: dump()
     */
    public void dumpToFile(String path) {
        dumpToFile_0(nativeObj, path);
    }

    /**
     * Converts string name of the layer to the integer identifier.
     *
     * @param layer automatically generated
     * @return id of the layer, or -1 if the layer wasn't found.
     */
    public int getLayerId(String layer) {
        return getLayerId_0(nativeObj, layer);
    }

    public List<String> getLayerNames() {
        return getLayerNames_0(nativeObj);
    }

    /**
     * Returns pointer to layer with specified id or name which the network use.
     *
     * @param layerId automatically generated
     * @return automatically generated
     */
    public Layer getLayer(int layerId) {
        return Layer.__fromPtr__(getLayer_0(nativeObj, layerId));
    }

    /**
     * @param layerName automatically generated
     * @return automatically generated
     * @deprecated Use int getLayerId(const String &amp;layer)
     */
    @Deprecated
    public Layer getLayer(String layerName) {
        return Layer.__fromPtr__(getLayer_1(nativeObj, layerName));
    }

    /**
     * @param layerId automatically generated
     * @return automatically generated
     * @deprecated to be removed
     */
    @Deprecated
    public Layer getLayer(DictValue layerId) {
        return Layer.__fromPtr__(getLayer_2(nativeObj, layerId.getNativeObjAddr()));
    }

    /**
     * Connects output of the first layer to input of the second layer.
     *
     * @param outPin descriptor of the first layer output.
     * @param inpPin descriptor of the second layer input.
     *               <p>
     *               Descriptors have the following template &lt;DFN&gt;&amp;lt;layer_name&amp;gt;[.input_number]&lt;/DFN&gt;:
     *               - the first part of the template &lt;DFN&gt;layer_name&lt;/DFN&gt; is string name of the added layer.
     *               If this part is empty then the network input pseudo layer will be used;
     *               - the second optional part of the template &lt;DFN&gt;input_number&lt;/DFN&gt;
     *               is either number of the layer input, either label one.
     *               If this part is omitted then the first layer input will be used.
     *               <p>
     *               SEE: setNetInputs(), Layer::inputNameToIndex(), Layer::outputNameToIndex()
     */
    public void connect(String outPin, String inpPin) {
        connect_0(nativeObj, outPin, inpPin);
    }

    /**
     * Sets outputs names of the network input pseudo layer.
     * <p>
     * Each net always has special own the network input pseudo layer with id=0.
     * This layer stores the user blobs only and don't make any computations.
     * In fact, this layer provides the only way to pass user data into the network.
     * As any other layer, this layer can label its outputs and this function provides an easy way to do this.
     *
     * @param inputBlobNames automatically generated
     */
    public void setInputsNames(List<String> inputBlobNames) {
        setInputsNames_0(nativeObj, inputBlobNames);
    }

    /**
     * Specify shape of network input.
     *
     * @param inputName automatically generated
     * @param shape     automatically generated
     */
    public void setInputShape(String inputName, MatOfInt shape) {
        Mat shape_mat = shape;
        setInputShape_0(nativeObj, inputName, shape_mat.nativeObj);
    }

    /**
     * Runs forward pass to compute output of layer with name {@code outputName}.
     *
     * @param outputName name for layer which output is needed to get
     * @return blob for first output of specified layer.
     * By default runs forward pass for the whole network.
     */
    public Mat forward(String outputName) {
        return new Mat(forward_0(nativeObj, outputName));
    }

    /**
     * Runs forward pass to compute output of layer with name {@code outputName}.
     *
     * @return blob for first output of specified layer.
     * By default runs forward pass for the whole network.
     */
    public Mat forward() {
        return new Mat(forward_1(nativeObj));
    }

    /**
     * Runs forward pass to compute output of layer with name {@code outputName}.
     *
     * @param outputBlobs contains all output blobs for specified layer.
     * @param outputName  name for layer which output is needed to get
     *                    If {@code outputName} is empty, runs forward pass for the whole network.
     */
    public void forward(List<Mat> outputBlobs, String outputName) {
        Mat outputBlobs_mat = new Mat();
        forward_2(nativeObj, outputBlobs_mat.nativeObj, outputName);
        Converters.Mat_to_vector_Mat(outputBlobs_mat, outputBlobs);
        outputBlobs_mat.release();
    }

    /**
     * Runs forward pass to compute output of layer with name {@code outputName}.
     *
     * @param outputBlobs contains all output blobs for specified layer.
     *                    If {@code outputName} is empty, runs forward pass for the whole network.
     */
    public void forward(List<Mat> outputBlobs) {
        Mat outputBlobs_mat = new Mat();
        forward_3(nativeObj, outputBlobs_mat.nativeObj);
        Converters.Mat_to_vector_Mat(outputBlobs_mat, outputBlobs);
        outputBlobs_mat.release();
    }

    /**
     * Runs forward pass to compute outputs of layers listed in {@code outBlobNames}.
     *
     * @param outputBlobs  contains blobs for first outputs of specified layers.
     * @param outBlobNames names for layers which outputs are needed to get
     */
    public void forward(List<Mat> outputBlobs, List<String> outBlobNames) {
        Mat outputBlobs_mat = new Mat();
        forward_4(nativeObj, outputBlobs_mat.nativeObj, outBlobNames);
        Converters.Mat_to_vector_Mat(outputBlobs_mat, outputBlobs);
        outputBlobs_mat.release();
    }

    /**
     * Returns a quantized Net from a floating-point Net.
     *
     * @param calibData    Calibration data to compute the quantization parameters.
     * @param inputsDtype  Datatype of quantized net's inputs. Can be CV_32F or CV_8S.
     * @param outputsDtype Datatype of quantized net's outputs. Can be CV_32F or CV_8S.
     * @param perChannel   Quantization granularity of quantized Net. The default is true, that means quantize model
     *                     in per-channel way (channel-wise). Set it false to quantize model in per-tensor way (or tensor-wise).
     * @return automatically generated
     */
    public Net quantize(List<Mat> calibData, int inputsDtype, int outputsDtype, boolean perChannel) {
        Mat calibData_mat = Converters.vector_Mat_to_Mat(calibData);
        return new Net(quantize_0(nativeObj, calibData_mat.nativeObj, inputsDtype, outputsDtype, perChannel));
    }

    /**
     * Returns a quantized Net from a floating-point Net.
     *
     * @param calibData    Calibration data to compute the quantization parameters.
     * @param inputsDtype  Datatype of quantized net's inputs. Can be CV_32F or CV_8S.
     * @param outputsDtype Datatype of quantized net's outputs. Can be CV_32F or CV_8S.
     *                     in per-channel way (channel-wise). Set it false to quantize model in per-tensor way (or tensor-wise).
     * @return automatically generated
     */
    public Net quantize(List<Mat> calibData, int inputsDtype, int outputsDtype) {
        Mat calibData_mat = Converters.vector_Mat_to_Mat(calibData);
        return new Net(quantize_1(nativeObj, calibData_mat.nativeObj, inputsDtype, outputsDtype));
    }

    /**
     * Returns input scale and zeropoint for a quantized Net.
     *
     * @param scales     output parameter for returning input scales.
     * @param zeropoints output parameter for returning input zeropoints.
     */
    public void getInputDetails(MatOfFloat scales, MatOfInt zeropoints) {
        Mat scales_mat = scales;
        Mat zeropoints_mat = zeropoints;
        getInputDetails_0(nativeObj, scales_mat.nativeObj, zeropoints_mat.nativeObj);
    }

    /**
     * Returns output scale and zeropoint for a quantized Net.
     *
     * @param scales     output parameter for returning output scales.
     * @param zeropoints output parameter for returning output zeropoints.
     */
    public void getOutputDetails(MatOfFloat scales, MatOfInt zeropoints) {
        Mat scales_mat = scales;
        Mat zeropoints_mat = zeropoints;
        getOutputDetails_0(nativeObj, scales_mat.nativeObj, zeropoints_mat.nativeObj);
    }

    /**
     * Compile Halide layers.
     *
     * @param scheduler Path to YAML file with scheduling directives.
     *                  SEE: setPreferableBackend
     *                  <p>
     *                  Schedule layers that support Halide backend. Then compile them for
     *                  specific target. For layers that not represented in scheduling file
     *                  or if no manual scheduling used at all, automatic scheduling will be applied.
     */
    public void setHalideScheduler(String scheduler) {
        setHalideScheduler_0(nativeObj, scheduler);
    }

    /**
     * Ask network to use specific computation backend where it supported.
     *
     * @param backendId backend identifier.
     *                  SEE: Backend
     *                  <p>
     *                  If OpenCV is compiled with Intel's Inference Engine library, DNN_BACKEND_DEFAULT
     *                  means DNN_BACKEND_INFERENCE_ENGINE. Otherwise it equals to DNN_BACKEND_OPENCV.
     */
    public void setPreferableBackend(int backendId) {
        setPreferableBackend_0(nativeObj, backendId);
    }

    /**
     * Ask network to make computations on specific target device.
     *
     * @param targetId target identifier.
     *                 SEE: Target
     *                 <p>
     *                 List of supported combinations backend / target:
     *                 |                        | DNN_BACKEND_OPENCV | DNN_BACKEND_INFERENCE_ENGINE | DNN_BACKEND_HALIDE |  DNN_BACKEND_CUDA |
     *                 |------------------------|--------------------|------------------------------|--------------------|-------------------|
     *                 | DNN_TARGET_CPU         |                  + |                            + |                  + |                   |
     *                 | DNN_TARGET_OPENCL      |                  + |                            + |                  + |                   |
     *                 | DNN_TARGET_OPENCL_FP16 |                  + |                            + |                    |                   |
     *                 | DNN_TARGET_MYRIAD      |                    |                            + |                    |                   |
     *                 | DNN_TARGET_FPGA        |                    |                            + |                    |                   |
     *                 | DNN_TARGET_CUDA        |                    |                              |                    |                 + |
     *                 | DNN_TARGET_CUDA_FP16   |                    |                              |                    |                 + |
     *                 | DNN_TARGET_HDDL        |                    |                            + |                    |                   |
     */
    public void setPreferableTarget(int targetId) {
        setPreferableTarget_0(nativeObj, targetId);
    }

    /**
     * Sets the new input value for the network
     *
     * @param blob        A new blob. Should have CV_32F or CV_8U depth.
     * @param name        A name of input layer.
     * @param scalefactor An optional normalization scale.
     * @param mean        An optional mean subtraction values.
     *                    SEE: connect(String, String) to know format of the descriptor.
     *                    <p>
     *                    If scale or mean values are specified, a final input blob is computed
     *                    as:
     *                    \(input(n,c,h,w) = scalefactor \times (blob(n,c,h,w) - mean_c)\)
     */
    public void setInput(Mat blob, String name, double scalefactor, Scalar mean) {
        setInput_0(nativeObj, blob.nativeObj, name, scalefactor, mean.val[0], mean.val[1], mean.val[2], mean.val[3]);
    }

    /**
     * Sets the new input value for the network
     *
     * @param blob        A new blob. Should have CV_32F or CV_8U depth.
     * @param name        A name of input layer.
     * @param scalefactor An optional normalization scale.
     *                    SEE: connect(String, String) to know format of the descriptor.
     *                    <p>
     *                    If scale or mean values are specified, a final input blob is computed
     *                    as:
     *                    \(input(n,c,h,w) = scalefactor \times (blob(n,c,h,w) - mean_c)\)
     */
    public void setInput(Mat blob, String name, double scalefactor) {
        setInput_1(nativeObj, blob.nativeObj, name, scalefactor);
    }

    /**
     * Sets the new input value for the network
     *
     * @param blob A new blob. Should have CV_32F or CV_8U depth.
     * @param name A name of input layer.
     *             SEE: connect(String, String) to know format of the descriptor.
     *             <p>
     *             If scale or mean values are specified, a final input blob is computed
     *             as:
     *             \(input(n,c,h,w) = scalefactor \times (blob(n,c,h,w) - mean_c)\)
     */
    public void setInput(Mat blob, String name) {
        setInput_2(nativeObj, blob.nativeObj, name);
    }

    /**
     * Sets the new input value for the network
     *
     * @param blob A new blob. Should have CV_32F or CV_8U depth.
     *             SEE: connect(String, String) to know format of the descriptor.
     *             <p>
     *             If scale or mean values are specified, a final input blob is computed
     *             as:
     *             \(input(n,c,h,w) = scalefactor \times (blob(n,c,h,w) - mean_c)\)
     */
    public void setInput(Mat blob) {
        setInput_3(nativeObj, blob.nativeObj);
    }

    /**
     * Sets the new value for the learned param of the layer.
     *
     * @param layer    name or id of the layer.
     * @param numParam index of the layer parameter in the Layer::blobs array.
     * @param blob     the new value.
     *                 SEE: Layer::blobs
     *                 <b>Note:</b> If shape of the new blob differs from the previous shape,
     *                 then the following forward pass may fail.
     */
    public void setParam(int layer, int numParam, Mat blob) {
        setParam_0(nativeObj, layer, numParam, blob.nativeObj);
    }

    public void setParam(String layerName, int numParam, Mat blob) {
        setParam_1(nativeObj, layerName, numParam, blob.nativeObj);
    }

    /**
     * Returns parameter blob of the layer.
     *
     * @param layer    name or id of the layer.
     * @param numParam index of the layer parameter in the Layer::blobs array.
     *                 SEE: Layer::blobs
     * @return automatically generated
     */
    public Mat getParam(int layer, int numParam) {
        return new Mat(getParam_0(nativeObj, layer, numParam));
    }

    /**
     * Returns parameter blob of the layer.
     *
     * @param layer name or id of the layer.
     *              SEE: Layer::blobs
     * @return automatically generated
     */
    public Mat getParam(int layer) {
        return new Mat(getParam_1(nativeObj, layer));
    }

    public Mat getParam(String layerName, int numParam) {
        return new Mat(getParam_2(nativeObj, layerName, numParam));
    }

    public Mat getParam(String layerName) {
        return new Mat(getParam_3(nativeObj, layerName));
    }

    /**
     * Returns indexes of layers with unconnected outputs.
     * <p>
     * FIXIT: Rework API to registerOutput() approach, deprecate this call
     *
     * @return automatically generated
     */
    public MatOfInt getUnconnectedOutLayers() {
        return MatOfInt.fromNativeAddr(getUnconnectedOutLayers_0(nativeObj));
    }

    /**
     * Returns names of layers with unconnected outputs.
     * <p>
     * FIXIT: Rework API to registerOutput() approach, deprecate this call
     *
     * @return automatically generated
     */
    public List<String> getUnconnectedOutLayersNames() {
        return getUnconnectedOutLayersNames_0(nativeObj);
    }

    /**
     * Computes FLOP for whole loaded model with specified input shapes.
     *
     * @param netInputShapes vector of shapes for all net inputs.
     * @return computed FLOP.
     */
    public long getFLOPS(List<MatOfInt> netInputShapes) {
        return getFLOPS_0(nativeObj, netInputShapes);
    }

    public long getFLOPS(MatOfInt netInputShape) {
        Mat netInputShape_mat = netInputShape;
        return getFLOPS_1(nativeObj, netInputShape_mat.nativeObj);
    }

    public long getFLOPS(int layerId, List<MatOfInt> netInputShapes) {
        return getFLOPS_2(nativeObj, layerId, netInputShapes);
    }

    public long getFLOPS(int layerId, MatOfInt netInputShape) {
        Mat netInputShape_mat = netInputShape;
        return getFLOPS_3(nativeObj, layerId, netInputShape_mat.nativeObj);
    }

    /**
     * Returns list of types for layer used in model.
     *
     * @param layersTypes output parameter for returning types.
     */
    public void getLayerTypes(List<String> layersTypes) {
        getLayerTypes_0(nativeObj, layersTypes);
    }

    /**
     * Returns count of layers of specified type.
     *
     * @param layerType type.
     * @return count of layers
     */
    public int getLayersCount(String layerType) {
        return getLayersCount_0(nativeObj, layerType);
    }

    public void getMemoryConsumption(MatOfInt netInputShape, long[] weights, long[] blobs) {
        Mat netInputShape_mat = netInputShape;
        double[] weights_out = new double[1];
        double[] blobs_out = new double[1];
        getMemoryConsumption_0(nativeObj, netInputShape_mat.nativeObj, weights_out, blobs_out);
        if (weights != null) weights[0] = (long) weights_out[0];
        if (blobs != null) blobs[0] = (long) blobs_out[0];
    }

    public void getMemoryConsumption(int layerId, List<MatOfInt> netInputShapes, long[] weights, long[] blobs) {
        double[] weights_out = new double[1];
        double[] blobs_out = new double[1];
        getMemoryConsumption_1(nativeObj, layerId, netInputShapes, weights_out, blobs_out);
        if (weights != null) weights[0] = (long) weights_out[0];
        if (blobs != null) blobs[0] = (long) blobs_out[0];
    }

    public void getMemoryConsumption(int layerId, MatOfInt netInputShape, long[] weights, long[] blobs) {
        Mat netInputShape_mat = netInputShape;
        double[] weights_out = new double[1];
        double[] blobs_out = new double[1];
        getMemoryConsumption_2(nativeObj, layerId, netInputShape_mat.nativeObj, weights_out, blobs_out);
        if (weights != null) weights[0] = (long) weights_out[0];
        if (blobs != null) blobs[0] = (long) blobs_out[0];
    }

    /**
     * Enables or disables layer fusion in the network.
     *
     * @param fusion true to enable the fusion, false to disable. The fusion is enabled by default.
     */
    public void enableFusion(boolean fusion) {
        enableFusion_0(nativeObj, fusion);
    }

    /**
     * Enables or disables the Winograd compute branch. The Winograd compute branch can speed up
     * 3x3 Convolution at a small loss of accuracy.
     *
     * @param useWinograd true to enable the Winograd compute branch. The default is true.
     */
    public void enableWinograd(boolean useWinograd) {
        enableWinograd_0(nativeObj, useWinograd);
    }

    /**
     * Returns overall time for inference and timings (in ticks) for layers.
     * <p>
     * Indexes in returned vector correspond to layers ids. Some layers can be fused with others,
     * in this case zero ticks count will be return for that skipped layers. Supported by DNN_BACKEND_OPENCV on DNN_TARGET_CPU only.
     *
     * @param timings vector for tick timings for all layers.
     * @return overall ticks for model inference.
     */
    public long getPerfProfile(MatOfDouble timings) {
        Mat timings_mat = timings;
        return getPerfProfile_0(nativeObj, timings_mat.nativeObj);
    }

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }

}
