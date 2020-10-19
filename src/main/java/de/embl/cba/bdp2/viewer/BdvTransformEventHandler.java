package de.embl.cba.bdp2.viewer;

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.TransformEventHandler;
import net.imglib2.ui.TransformEventHandlerFactory;
import net.imglib2.ui.TransformListener;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.ScrollBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;
import org.scijava.ui.behaviour.util.TriggerBehaviourBindings;

public class BdvTransformEventHandler implements TransformEventHandler<AffineTransform3D> {
    protected final AffineTransform3D affine = new AffineTransform3D();
    private TransformListener<AffineTransform3D> listener;
    protected final AffineTransform3D affineDragStart = new AffineTransform3D();
    protected double oX;
    protected double oY;
    protected int axis = 0;
    protected int canvasW = 1;
    protected int canvasH = 1;
    protected int centerX = 0;
    protected int centerY = 0;
    protected final Behaviours behaviours;
    protected static final double step = 0.017453292519943295D;
    protected static final String NL = System.getProperty("line.separator");
    protected static final String helpString;


    public BdvTransformEventHandler( TransformListener<AffineTransform3D> listener, InputTriggerConfig config, double[] voxelSpacing ) {

        // TODO: use voxelSpacing[ 2 ] to set z-step size

        this.listener = listener;
        String DRAG_TRANSLATE = "drag translate";
        String ZOOM_NORMAL = "scroll zoom";
        String SELECT_AXIS_X = "axis x";
        String SELECT_AXIS_Y = "axis y";
        String SELECT_AXIS_Z = "axis z";
        //double[] speed = new double[]{1.0D, 10.0D, 0.1D};
        double[] speed = new double[]{0.5, 2, 0.1};
        String[] SPEED_NAME = new String[]{"", " fast", " slow"};
        String[] speedMod = new String[]{"", "align ", "ctrl "};
        String DRAG_ROTATE = "drag rotate";
        String SCROLL_Z = "scroll browse z";
        String ROTATE_LEFT = "rotate left";
        String ROTATE_RIGHT = "rotate right";
        String KEY_ZOOM_IN = "zoom in";
        String KEY_ZOOM_OUT = "zoom out";
        String KEY_FORWARD_Z = "forward z";
        String KEY_BACKWARD_Z = "backward z";
        this.behaviours = new Behaviours(config, new String[]{"bdv"});
        this.behaviours.behaviour(new BdvTransformEventHandler.TranslateXY(), "drag translate", new String[]{"button1", "button3"});
        this.behaviours.behaviour(new BdvTransformEventHandler.Zoom(speed[0]), "scroll zoom", new String[]{"meta scroll", "ctrl align scroll"});
        this.behaviours.behaviour(new BdvTransformEventHandler.SelectRotationAxis(0), SELECT_AXIS_X, new String[]{"X"});
        this.behaviours.behaviour(new BdvTransformEventHandler.SelectRotationAxis(1), SELECT_AXIS_Y, new String[]{"Y"});
        this.behaviours.behaviour(new BdvTransformEventHandler.SelectRotationAxis(2), SELECT_AXIS_Z, new String[]{"Z"});

        for(int s = 0; s < 3; ++s) {
            //this.behaviours.behaviour(new BdvTransformEventHandler.Rotate(speed[s]), "drag rotate" + SPEED_NAME[s], new String[]{speedMod[s] + "button1"});
            this.behaviours.behaviour(new BdvTransformEventHandler.TranslateZ(speed[s]), SCROLL_Z + SPEED_NAME[s], new String[]{speedMod[s] + "scroll"});
            //this.behaviours.behaviour(new BdvTransformEventHandler.KeyRotate(speed[s]), "rotate left" + SPEED_NAME[s], new String[]{speedMod[s] + "LEFT"});
            //this.behaviours.behaviour(new BdvTransformEventHandler.KeyRotate(-speed[s]), ROTATE_RIGHT + SPEED_NAME[s], new String[]{speedMod[s] + "RIGHT"});
            this.behaviours.behaviour(new BdvTransformEventHandler.KeyZoom(speed[s]), KEY_ZOOM_IN + SPEED_NAME[s], new String[]{speedMod[s] + "UP"});
            this.behaviours.behaviour(new BdvTransformEventHandler.KeyZoom(-speed[s]), KEY_ZOOM_OUT + SPEED_NAME[s], new String[]{speedMod[s] + "DOWN"});
            this.behaviours.behaviour(new BdvTransformEventHandler.KeyTranslateZ(speed[s]), KEY_FORWARD_Z + SPEED_NAME[s], new String[]{speedMod[s] + "COMMA"});
            this.behaviours.behaviour(new BdvTransformEventHandler.KeyTranslateZ(-speed[s]), KEY_BACKWARD_Z + SPEED_NAME[s], new String[]{speedMod[s] + "PERIOD"});
        }

    }

    public void install(TriggerBehaviourBindings bindings) {
        this.behaviours.install(bindings, "transform");
    }

    public AffineTransform3D getTransform() {
        AffineTransform3D var1 = this.affine;
        synchronized(this.affine) {
            return this.affine.copy();
        }
    }

    public void setTransform(AffineTransform3D transform) {
        AffineTransform3D var2 = this.affine;
        synchronized(this.affine) {
            this.affine.set(transform);
        }
    }

    public void setCanvasSize(int width, int height, boolean updateTransform) {
        if (width != 0 && height != 0) {
            if (updateTransform) {
                AffineTransform3D var4 = this.affine;
                synchronized(this.affine) {
                    this.affine.set(this.affine.get(0, 3) - (double)(this.canvasW / 2), 0, 3);
                    this.affine.set(this.affine.get(1, 3) - (double)(this.canvasH / 2), 1, 3);
                    this.affine.scale((double)width / (double)this.canvasW);
                    this.affine.set(this.affine.get(0, 3) + (double)(width / 2), 0, 3);
                    this.affine.set(this.affine.get(1, 3) + (double)(height / 2), 1, 3);
                    this.notifyListener();
                }
            }

            this.canvasW = width;
            this.canvasH = height;
            this.centerX = width / 2;
            this.centerY = height / 2;
        }
    }

    public void setTransformListener(TransformListener<AffineTransform3D> transformListener) {
        this.listener = transformListener;
    }

    public String getHelpString() {
        return helpString;
    }

    private void notifyListener() {
        if (this.listener != null) {
            this.listener.transformChanged(this.affine);
        }

    }

    private void scale(double s, double x, double y) {
        this.affine.set(this.affine.get(0, 3) - x, 0, 3);
        this.affine.set(this.affine.get(1, 3) - y, 1, 3);
        this.affine.scale(s);
        this.affine.set(this.affine.get(0, 3) + x, 0, 3);
        this.affine.set(this.affine.get(1, 3) + y, 1, 3);
    }

    private void rotate(int axis, double d) {
        this.affine.set(this.affine.get(0, 3) - (double)this.centerX, 0, 3);
        this.affine.set(this.affine.get(1, 3) - (double)this.centerY, 1, 3);
        this.affine.rotate(axis, d);
        this.affine.set(this.affine.get(0, 3) + (double)this.centerX, 0, 3);
        this.affine.set(this.affine.get(1, 3) + (double)this.centerY, 1, 3);
    }

    static {
        helpString = "Mouse control:" + NL + " " + NL + "Pan and tilt the volume by left-click and dragging the image in the canvas, " + NL + "move the volume by middle-or-right-click and dragging the image in the canvas, " + NL + "browse alongside the z-axis using the mouse-wheel, and" + NL + "zoom in and out using the mouse-wheel holding CTRL+SHIFT or META." + NL + " " + NL + "Key control:" + NL + " " + NL + "X - Select x-axis as rotation axis." + NL + "Y - Select y-axis as rotation axis." + NL + "Z - Select z-axis as rotation axis." + NL + "CURSOR LEFT - Rotate clockwise around the choosen rotation axis." + NL + "CURSOR RIGHT - Rotate counter-clockwise around the choosen rotation axis." + NL + "CURSOR UP - Zoom in." + NL + "CURSOR DOWN - Zoom out." + NL + "./> - Forward alongside z-axis." + NL + ",/< - Backward alongside z-axis." + NL + "SHIFT - Rotate and browse 10x faster." + NL + "CTRL - Rotate and browse 10x slower.";
    }

    private class KeyTranslateZ implements ClickBehaviour {
        private final double speed;

        public KeyTranslateZ(double speed) {
            this.speed = speed;
        }

        public void click(int x, int y) {
            AffineTransform3D var3 = BdvTransformEventHandler.this.affine;
            synchronized(BdvTransformEventHandler.this.affine) {
                BdvTransformEventHandler.this.affine.set(BdvTransformEventHandler.this.affine.get(2, 3) + this.speed, 2, 3);
                BdvTransformEventHandler.this.notifyListener();
            }
        }
    }

    private class KeyZoom implements ClickBehaviour {
        private final double dScale;

        public KeyZoom(double speed) {
            if (speed > 0.0D) {
                this.dScale = 1.0D + 0.1D * speed;
            } else {
                this.dScale = 1.0D / (1.0D - 0.1D * speed);
            }

        }

        public void click(int x, int y) {
            AffineTransform3D var3 = BdvTransformEventHandler.this.affine;
            synchronized(BdvTransformEventHandler.this.affine) {
                BdvTransformEventHandler.this.scale(this.dScale, (double) BdvTransformEventHandler.this.centerX, (double) BdvTransformEventHandler.this.centerY);
                BdvTransformEventHandler.this.notifyListener();
            }
        }
    }

    private class KeyRotate implements ClickBehaviour {
        private final double speed;

        public KeyRotate(double speed) {
            this.speed = speed;
        }

        public void click(int x, int y) {
            AffineTransform3D var3 = BdvTransformEventHandler.this.affine;
            synchronized(BdvTransformEventHandler.this.affine) {
                BdvTransformEventHandler.this.rotate(BdvTransformEventHandler.this.axis, 0.017453292519943295D * this.speed);
                BdvTransformEventHandler.this.notifyListener();
            }
        }
    }

    private class SelectRotationAxis implements ClickBehaviour {
        private final int axis;

        public SelectRotationAxis(int axis) {
            this.axis = axis;
        }

        public void click(int x, int y) {
            BdvTransformEventHandler.this.axis = this.axis;
        }
    }

    private class Zoom implements ScrollBehaviour {
        private final double speed;

        public Zoom(double speed) {
            this.speed = speed;
        }

        public void scroll(double wheelRotation, boolean isHorizontal, int x, int y) {
            AffineTransform3D var6 = BdvTransformEventHandler.this.affine;
            synchronized(BdvTransformEventHandler.this.affine) {
                double s = this.speed * wheelRotation;
                double dScale = 1.05D;
                if (s > 0.0D) {
                    BdvTransformEventHandler.this.scale(0.9523809523809523D, (double)x, (double)y);
                } else {
                    BdvTransformEventHandler.this.scale(1.05D, (double)x, (double)y);
                }

                BdvTransformEventHandler.this.notifyListener();
            }
        }
    }

    private class TranslateZ implements ScrollBehaviour {
        private final double speed;

        public TranslateZ(double speed) {
            this.speed = speed;
        }

        public void scroll(double wheelRotation, boolean isHorizontal, int x, int y) {
            AffineTransform3D var6 = BdvTransformEventHandler.this.affine;
            synchronized(BdvTransformEventHandler.this.affine) {
                double dZ = this.speed * -wheelRotation;
                BdvTransformEventHandler.this.affine.set(BdvTransformEventHandler.this.affine.get(2, 3) - dZ, 2, 3);
                BdvTransformEventHandler.this.notifyListener();
            }
        }
    }

    private class TranslateXY implements DragBehaviour {
        private TranslateXY() {
        }

        public void init(int x, int y) {
            AffineTransform3D var3 = BdvTransformEventHandler.this.affine;
            synchronized(BdvTransformEventHandler.this.affine) {
                BdvTransformEventHandler.this.oX = (double)x;
                BdvTransformEventHandler.this.oY = (double)y;
                BdvTransformEventHandler.this.affineDragStart.set(BdvTransformEventHandler.this.affine);
            }
        }

        public void drag(int x, int y) {
            AffineTransform3D var3 = BdvTransformEventHandler.this.affine;
            synchronized(BdvTransformEventHandler.this.affine) {
                double dX = BdvTransformEventHandler.this.oX - (double)x;
                double dY = BdvTransformEventHandler.this.oY - (double)y;
                BdvTransformEventHandler.this.affine.set(BdvTransformEventHandler.this.affineDragStart);
                BdvTransformEventHandler.this.affine.set(BdvTransformEventHandler.this.affine.get(0, 3) - dX, 0, 3);
                BdvTransformEventHandler.this.affine.set(BdvTransformEventHandler.this.affine.get(1, 3) - dY, 1, 3);
                BdvTransformEventHandler.this.notifyListener();
            }
        }

        public void end(int x, int y) {
        }
    }

    private class Rotate implements DragBehaviour {
        private final double speed;

        public Rotate(double speed) {
            this.speed = speed;
        }

        public void init(int x, int y) {
            AffineTransform3D var3 = BdvTransformEventHandler.this.affine;
            synchronized(BdvTransformEventHandler.this.affine) {
                BdvTransformEventHandler.this.oX = (double)x;
                BdvTransformEventHandler.this.oY = (double)y;
                BdvTransformEventHandler.this.affineDragStart.set(BdvTransformEventHandler.this.affine);
            }
        }

        public void drag(int x, int y) {
            AffineTransform3D var3 = BdvTransformEventHandler.this.affine;
            synchronized(BdvTransformEventHandler.this.affine) {
                double dX = BdvTransformEventHandler.this.oX - (double)x;
                double dY = BdvTransformEventHandler.this.oY - (double)y;
                BdvTransformEventHandler.this.affine.set(BdvTransformEventHandler.this.affineDragStart);
                BdvTransformEventHandler.this.affine.set(BdvTransformEventHandler.this.affine.get(0, 3) - BdvTransformEventHandler.this.oX, 0, 3);
                BdvTransformEventHandler.this.affine.set(BdvTransformEventHandler.this.affine.get(1, 3) - BdvTransformEventHandler.this.oY, 1, 3);
                double v = 0.017453292519943295D * this.speed;
                BdvTransformEventHandler.this.affine.rotate(0, -dY * v);
                BdvTransformEventHandler.this.affine.rotate(1, dX * v);
                BdvTransformEventHandler.this.affine.set(BdvTransformEventHandler.this.affine.get(0, 3) + BdvTransformEventHandler.this.oX, 0, 3);
                BdvTransformEventHandler.this.affine.set(BdvTransformEventHandler.this.affine.get(1, 3) + BdvTransformEventHandler.this.oY, 1, 3);
                BdvTransformEventHandler.this.notifyListener();
            }
        }

        public void end(int x, int y) {
        }
    }

    public static class BehaviourTransformEventHandler3DFactory implements TransformEventHandlerFactory<AffineTransform3D> {

        final double[] voxelSpacing;

        private InputTriggerConfig config = new InputTriggerConfig();

        public BehaviourTransformEventHandler3DFactory( double[] voxelSpacing ) { //TODO  :add new arguments for voxel size--ashis
            this.voxelSpacing = voxelSpacing;
        }

        public void setConfig(InputTriggerConfig config) {
            this.config = config;
        }

        public BdvTransformEventHandler create(TransformListener<AffineTransform3D> transformListener) {
            return new BdvTransformEventHandler( transformListener, this.config, voxelSpacing);
        }
    }
}
