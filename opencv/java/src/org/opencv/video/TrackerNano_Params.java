//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.video;


// C++: class Params

public class TrackerNano_Params {

    protected final long nativeObj;

    protected TrackerNano_Params(long addr) {
        nativeObj = addr;
    }

    public TrackerNano_Params() {
        nativeObj = TrackerNano_Params_0();
    }

    // internal usage only
    public static TrackerNano_Params __fromPtr__(long addr) {
        return new TrackerNano_Params(addr);
    }

    //
    // C++:   cv::TrackerNano::Params::Params()
    //

    // C++:   cv::TrackerNano::Params::Params()
    private static native long TrackerNano_Params_0();


    //
    // C++: string TrackerNano_Params::backbone
    //

    // C++: string TrackerNano_Params::backbone
    private static native String get_backbone_0(long nativeObj);


    //
    // C++: void TrackerNano_Params::backbone
    //

    // C++: void TrackerNano_Params::backbone
    private static native void set_backbone_0(long nativeObj, String backbone);


    //
    // C++: string TrackerNano_Params::neckhead
    //

    // C++: string TrackerNano_Params::neckhead
    private static native String get_neckhead_0(long nativeObj);


    //
    // C++: void TrackerNano_Params::neckhead
    //

    // C++: void TrackerNano_Params::neckhead
    private static native void set_neckhead_0(long nativeObj, String neckhead);


    //
    // C++: int TrackerNano_Params::backend
    //

    // C++: int TrackerNano_Params::backend
    private static native int get_backend_0(long nativeObj);


    //
    // C++: void TrackerNano_Params::backend
    //

    // C++: void TrackerNano_Params::backend
    private static native void set_backend_0(long nativeObj, int backend);


    //
    // C++: int TrackerNano_Params::target
    //

    // C++: int TrackerNano_Params::target
    private static native int get_target_0(long nativeObj);


    //
    // C++: void TrackerNano_Params::target
    //

    // C++: void TrackerNano_Params::target
    private static native void set_target_0(long nativeObj, int target);

    // native support for java finalize()
    private static native void delete(long nativeObj);

    public long getNativeObjAddr() {
        return nativeObj;
    }

    public String get_backbone() {
        return get_backbone_0(nativeObj);
    }

    public void set_backbone(String backbone) {
        set_backbone_0(nativeObj, backbone);
    }

    public String get_neckhead() {
        return get_neckhead_0(nativeObj);
    }

    public void set_neckhead(String neckhead) {
        set_neckhead_0(nativeObj, neckhead);
    }

    public int get_backend() {
        return get_backend_0(nativeObj);
    }

    public void set_backend(int backend) {
        set_backend_0(nativeObj, backend);
    }

    public int get_target() {
        return get_target_0(nativeObj);
    }

    public void set_target(int target) {
        set_target_0(nativeObj, target);
    }

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }

}
