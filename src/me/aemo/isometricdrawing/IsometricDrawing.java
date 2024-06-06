package me.aemo.isometricdrawing;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.util.YailDictionary;
import com.google.appinventor.components.runtime.util.YailList;
import io.fabianterhorst.isometric.*;
import io.fabianterhorst.isometric.paths.Circle;
import io.fabianterhorst.isometric.paths.Rectangle;
import io.fabianterhorst.isometric.paths.Star;
import io.fabianterhorst.isometric.shapes.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public class IsometricDrawing extends AndroidNonvisibleComponent {

  private final ComponentContainer container;
  private IsometricView isometricView;

  private boolean sort = true;
  private boolean cull = false;
  private boolean boundsCheck = false;
  private boolean reverseSortForLookup = false;
  private boolean touchRadiusLookup = false;

  private double touchRadius = 1;
  private int bgColor = android.graphics.Color.TRANSPARENT;

  public IsometricDrawing(ComponentContainer container) {
    super(container.$form());
    this.container = container;
  }
  private Class<?> getType(Object main) {
    switch (main.getClass().getSimpleName()) {
      case "Circle":
        return Circle.class;
      case "Rectangle":
        return Rectangle.class;
      case "Star":
        return Star.class;
      case "Cylinder":
        return Cylinder.class;
      case "Knot":
        return Knot.class;
      case "Octahedron":
        return Octahedron.class;
      case "Prism":
        return Prism.class;
      case "Pyramid":
        return Pyramid.class;
      case "Stairs":
        return Stairs.class;
      default:
        return main.getClass();
    }
  }


  @SimpleEvent(description = "This event is triggered when an error occurs.")
  public void OnErrorOccurred(String error, String from) {
    EventDispatcher.dispatchEvent(this, "OnErrorOccurred", error, from);
  }

  @SimpleProperty(description = "Whether to sort the drawing items. This greatly improves drawing speed.\nPaths must be defined in a counter-clockwise rotation order.")
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
  public void Sort(boolean sort) {
    this.sort = sort;
  }

  @SimpleProperty(description = "Whether to cull the drawing items. This improves drawing speed by not considering items that are outside of view bounds.")
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
  public void Cull(boolean cull) {
    this.cull = cull;
  }

  @SimpleProperty(description = "Whether to perform bounds checking. This improves drawing speed by not considering items that are outside of view bounds.")
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
  public void BoundsCheck(boolean boundsCheck) {
    this.boundsCheck = boundsCheck;
  }

  @SimpleProperty(description = "Whether to reverse the sort of the items array for drawing lookup. This allows the items array to be reversed when looking up which drawing item was touched.")
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
  public void ReverseSortForLookup(boolean reverseSortForLookup) {
    this.reverseSortForLookup = reverseSortForLookup;
  }

  @SimpleProperty(description = "Whether to allow the click lookup to consider a touch region defined by a circle instead of a fixed point.")
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
  public void TouchRadiusLookup(boolean touchRadiusLookup) {
    this.touchRadiusLookup = touchRadiusLookup;
  }

  @SimpleProperty(description = "The radius of the circular region with the center being the click event location. The size is in screen pixels.")
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER, defaultValue = "1")
  public void TouchRadius(int touchRadius) {
    this.touchRadius = touchRadius;
  }

  @SimpleProperty(description = "The background color of the view.")
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = DEFAULT_VALUE_COLOR_DEFAULT)
  public void BackgroundColor(int bgColor) {
    this.bgColor = bgColor;
  }

  @SimpleFunction(description = "Initializes the Isometric Drawing component.")
  public void Initialize(AndroidViewComponent view) {
    isometricView = new IsometricView((Context) container);
    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    isometricView.setLayoutParams(params);
    isometricView.setBackgroundColor(bgColor);
    isometricView.setSort(sort);
    isometricView.setCull(cull);
    isometricView.setBoundsCheck(boundsCheck);
    isometricView.setReverseSortForLookup(reverseSortForLookup);
    isometricView.setTouchRadiusLookup(touchRadiusLookup);
    isometricView.setTouchRadius(touchRadius);
    isometricView.setClickListener(new IsometricView.OnItemClickListener() {
      @Override
      public void onClick(@NonNull @NotNull Isometric.Item item) {
        OnClick(getType(item.getOriginalShape()).getSimpleName(), pathToList(item.getPath()));
      }
    });

    ViewGroup vg = (ViewGroup) view.getView();
    if (vg.getChildCount() > 1) vg.removeAllViews();
    vg.addView(isometricView, params);
  }

  private YailDictionary pathToList(Path path) {
    Point[] points = path.getPoints();
    YailDictionary dictionary = new YailDictionary();
    for (int i = 0; i < points.length; i++) {
      Point point = points[i];
      YailList coordinates = YailList.makeList(new Object[]{point.getX(), point.getY(), point.getZ()});
      dictionary.put(i + 1, coordinates);
    }
    return dictionary;
  }

  @SimpleEvent(description = "This event is triggered when a shape is clicked.")
  public void OnClick(String shapeType, Object pathList) {
    EventDispatcher.dispatchEvent(this, "OnClick", shapeType, pathList);
  }

  @SimpleFunction(description = "Clears the Isometric Drawing view.")
  public void Clear() {
    if (isometricView != null) {
      ViewGroup vg = (ViewGroup) isometricView.getParent();
      if (vg != null) {
        vg.removeView(isometricView);
      } else {
        OnErrorOccurred("Can't get parent of isometric drawing view.", "Clear");
        return;
      }
      isometricView.clear();
      isometricView = null;
      container.$context().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          OnClear();
        }
      });
    } else {
      OnErrorOccurred("Isometric Drawing view is null", "Clear");
    }
  }

  @SimpleEvent
  public void OnClear() {
    EventDispatcher.dispatchEvent(this, "OnClear");
  }


  @SimpleFunction(description = "Adds a shape to the Isometric Drawing view.")
  public void AddShape(Object shape, Object color) {
    if (isometricView != null) {
      if (shape instanceof Shape){
        isometricView.add((Shape) shape, (Color) color);
      } else {
        OnErrorOccurred("Invalid shape type for add shape", "AddShape");
      }
    }
  }

  @SimpleFunction(description = "Adds a path to the Isometric Drawing view.")
  public void AddPath(Object path, Object color) {
    if (isometricView != null) {
      if (path instanceof Path) {
        isometricView.add((Path) path, (Color) color);
      } else {
        OnErrorOccurred("Invalid path type for add path", "AddPath");
      }
    }
  }

  @SimpleFunction(description = "Creates a color with the specified RGB values.")
  public Object CreateColor(int color) {
    return new Color(ColorUtils.getRed(color), ColorUtils.getGreen(color), ColorUtils.getBlue(color));
  }

  @SimpleFunction(description = "Creates a color with the specified RGB values and alpha value.")
  public Object CreateColorWithAlpha(int color) {
    return new Color(ColorUtils.getRed(color), ColorUtils.getGreen(color), ColorUtils.getBlue(color), ColorUtils.getAlpha(color));
  }

  @SimpleFunction(description = "Creates a color with the specified RGBA values.")
  public Object CreateColorRGBA(int r, int g, int b, int a) {
    return new Color(r, g, b, a);
  }

  @SimpleFunction(description = "Creates a color with the specified RGB values.")
  public Object CreateColorRGB(int r, int g, int b) {
    return new Color(r, g, b);
  }

  @SimpleProperty(description = "A point representing the origin.")
  public Object PointOrigin() {
    return Point.ORIGIN;
  }

  @SimpleFunction(description = "Creates a point with the specified x and y coordinates.")
  public Object CreatePointXY(int x, int y) {
    return new Point(x, y);
  }

  @SimpleFunction(description = "Creates a point with the specified x, y, and z coordinates.")
  public Object CreatePointXYZ(int x, int y, int z) {
    return new Point(x, y, z);
  }

  @SimpleFunction(description = "Creates a circle shape with the specified origin, radius, and number of vertices.")
  public Object CreateCircle(Object origin, double radius, double vertices) {
    if (origin instanceof Point) {
      return new Circle((Point) origin, radius, vertices);
    } else {
      OnErrorOccurred("Invalid point type for creating circle path", "CreateCircle");
      return null;
    }
  }

  @SimpleFunction(description = "Creates a rectangle shape with the specified origin, width, and height.")
  public Object CreateRectangle(Object origin, int width, int height) {
    if (origin instanceof Point) {
      return new Rectangle((Point) origin, width, height);
    } else {
      OnErrorOccurred("Invalid point type for creating rectangle path", "CreateRectangle");
      return null;
    }
  }

  @SimpleFunction(description = "Creates a star shape with the specified origin, outer radius, inner radius, and number of points.")
  public Object CreateStar(Object origin, double outerRadius, double innerRadius, int points) {
    if (origin instanceof Point) {
      return new Star((Point) origin, outerRadius, innerRadius, points);
    } else {
      OnErrorOccurred("Invalid point type for creating star path", "CreateStar");
      return null;
    }
  }




  @SimpleFunction(description = "Creates a path with the specified list of points.")
  public Object CreatePath(YailList points) {
    Object[] objects = points.toArray();
    Point[] list = new Point[objects.length];
    for (int i = 0; i < objects.length; i++) {
      if (objects[i] instanceof Point) {
        list[i] = (Point) objects[i];
      } else {
        OnErrorOccurred("Invalid point type for creating path", "CreatePath");
      }
    }
    return new Path(list);
  }

  @SimpleFunction(description = "Creates an extruded shape from the specified path and height.")
  public Object ShapeExtrude(Object path, int height) {
    return Shape.extrude((Path) path, height);
  }

  @SimpleFunction(description = "Creates a cylinder shape with the specified origin, radius, number of vertices, and height.")
  public Object CreateCylinder(Object originPoint, int radius, int vertices, int height) {
    if (originPoint instanceof Point) {
      return new Cylinder((Point) originPoint, radius, vertices, height);
    } else {
      OnErrorOccurred("Error Point Type", "CreateCylinder");
    }
    return null;
  }

  @SimpleFunction(description = "Creates a knot shape with the specified origin.")
  public Object CreateKnot(Object originPoint) {
    if (originPoint instanceof Point) {
      return new Knot((Point) originPoint);
    } else {
      OnErrorOccurred("Error Point Type", "CreateKnot");
    }
    return null;
  }

  @SimpleFunction(description = "Creates an octahedron shape with the specified origin.")
  public Object CreateOctahedron(Object originPoint) {
    if (originPoint instanceof Point) {
      return new Octahedron((Point) originPoint);
    } else {
      OnErrorOccurred("Error Point Type", "CreateOctahedron");
    }
    return null;
  }

  @SimpleFunction(description = "Creates a prism shape with the specified origin, dx, dy, and dz.")
  public Object CreatePrism(Object originPoint, int dx, int dy, int dz) {
    if (originPoint instanceof Point) {
      return new Prism((Point) originPoint, dx, dy, dz);
    } else {
      OnErrorOccurred("Error Point Type", "CreatePrism");
    }
    return null;
  }

  @SimpleFunction(description = "Creates a pyramid shape with the specified origin, dx, dy, and dz.")
  public Object CreatePyramid(Object originPoint, int dx, int dy, int dz) {
    if (originPoint instanceof Point) {
      return new Pyramid((Point) originPoint, dx, dy, dz);
    } else {
      OnErrorOccurred("Error Point Type", "CreatePyramid");
    }
    return null;
  }

  @SimpleFunction(description = "Creates a stairs shape with the specified origin and step count.")
  public Object CreateStairs(Object originPoint, int stepCount) {
    if (originPoint instanceof Point) {
      return new Stairs((Point) originPoint, stepCount);
    } else {
      OnErrorOccurred("Error Point Type", "CreateStairs");
    }
    return null;
  }

  @SimpleProperty(description = "Default value for various properties.")
  public int DefaultValue() {
    return 1;
  }

  @SimpleProperty(description = "The value of PI (π).")
  public double MathPI() {
    return Math.PI;
  }







  /*
  @SimpleFunction(description = "Translate a point or shape or path from a given dx, dy, and dz")
  public Object WithTranslate(Object main, int dx, int dy, int dz) {
    if (main instanceof Point) {
      return ((Point) main).translate(dx, dy, dz);
    } else if (main instanceof Shape) {
      return ((Shape) main).translate(dx, dy, dz);
    } else if (main instanceof Path) {
      return ((Path) main).translate(dx, dy, dz);
    } else {
      OnErrorOccurred("Error Point Type", "Translate");
      return null;
    }
  }
  */
  @SimpleFunction(description = "Translates a point, shape, or path by the given dx, dy, and dz.")
  public Object Translate(Object main, double dx, double dy, double dz) {
    if (main != null) {
      try {
        Class<?> clazz = getType(main);
        Method method = clazz.getMethod("translate", double.class, double.class, double.class);
        return method.invoke(main, dx, dy, dz);
      } catch (NoSuchMethodException e) {
        OnErrorOccurred(e.getMessage(), "Translate ~ Method");
      } catch (Exception e) {
        OnErrorOccurred("Error invoking method: " + e.getMessage(), "Translate");
      }
    } else {
      OnErrorOccurred("Invalid main object for translation", "Translate");
    }
    return null;
  }







  /*
  @SimpleFunction(description = "Scale a point or shape or path from a given originPoint, dx, dy, and dz")
  public Object WithScale(Object main, Object originPoint, int dx, int dy, int dz) {
    if (main instanceof Point && originPoint instanceof Point) {
      return ((Point) main).scale((Point) originPoint, dx, dy, dz);
    } else if (main instanceof Shape && originPoint instanceof Point) {
      return ((Shape) main).scale((Point) originPoint, dx, dy, dz);
    } else if (main instanceof Path && originPoint instanceof Point) {
      return ((Path) main).scale((Point) originPoint, dx, dy, dz);
    } else {
      OnErrorOccurred("Error Scale Type", "Scale");
      return null;
    }
  }
  */
  @SimpleFunction(description = "Scales a point, shape, or path from a given originPoint, dx, dy, and dz.")
  public Object Scale(Object main, Object originPoint, double dx, double dy, double dz) {
    if (main != null && originPoint instanceof Point) {
      try {
        Class<?> clazz = getType(main);
        Method method = clazz.getMethod("scale", Point.class, double.class, double.class, double.class);
        return method.invoke(main, originPoint, dx, dy, dz);
      } catch (NoSuchMethodException e) {
        OnErrorOccurred(e.getMessage(), "Scale ~ Method");
      } catch (Exception e) {
        OnErrorOccurred("Error invoking method: " + e.getMessage(), "Scale");
      }
    } else {
      OnErrorOccurred("Invalid main or originPoint type for scaling", "Scale");
    }
    return null;
  }




  /*
  @SimpleFunction(description = "Rotate about origin on the X axis")
  public Object WithRotateX(Object main, Object originPoint, int angle){
    if (main instanceof Point && originPoint instanceof Point){
      return ((Point) main).rotateX((Point) originPoint, angle);
    } else if (main instanceof Shape && originPoint instanceof Point) {
      return ((Shape) main).rotateX((Point) originPoint, angle);
    } else if (main instanceof Path) {
      return ((Path) main).rotateX((Point) originPoint, angle);
    } else {
      OnErrorOccurred("Error Type", "RotateX");
      return null;
    }
  }
  */
  @SimpleFunction(description = "Rotates a point, shape, or path about the origin on the X axis.")
  public Object RotateX(Object main, Object originPoint, double angle) {
    if (main != null && originPoint instanceof Point) {
      try {
        Class<?> clazz = getType(main);
        Method method = clazz.getMethod("rotateX", Point.class, double.class);
        return method.invoke(main, originPoint, angle);
      } catch (NoSuchMethodException e) {
        OnErrorOccurred(e.getMessage(), "RotateX ~ Method");
      } catch (Exception e) {
        OnErrorOccurred("Error invoking method: " + e.getMessage(), "RotateX");
      }
    } else {
      OnErrorOccurred("Invalid main or originPoint type for rotating X", "RotateX");
    }
    return null;
  }




  /*
  @SimpleFunction(description = "Rotate about origin on the Y axis")
  public Object WithRotateY(Object main, Object originPoint, int angle){
    if (main instanceof Point && originPoint instanceof Point){
      return ((Point) main).rotateY((Point) originPoint, angle);
    } else if (main instanceof Shape && originPoint instanceof Point) {
      return ((Shape) main).rotateY((Point) originPoint, angle);
    } else if (main instanceof Path) {
      return ((Path) main).rotateY((Point) originPoint, angle);
    } else {
      OnErrorOccurred("Error Type", "RotateY");
      return null;
    }
  }
  */

  @SimpleFunction(description = "Rotates a point, shape, or path about the origin on the Y axis.")
  public Object RotateY(Object main, Object originPoint, double angle) {
    if (main != null && originPoint instanceof Point) {
      try {
        Class<?> clazz = getType(main);
        Method method = clazz.getMethod("rotateY", Point.class, double.class);
        return method.invoke(main, originPoint, angle);
      } catch (NoSuchMethodException e) {
        OnErrorOccurred(e.getMessage(), "RotateY ~ Method");
      } catch (Exception e) {
        OnErrorOccurred("Error invoking method: " + e.getMessage(), "RotateY");
      }
    } else {
      OnErrorOccurred("Invalid main or originPoint type for rotating Y", "RotateY");
    }
    return null;
  }



  /*
  @SimpleFunction(description = "Rotate about origin on the Z axis")
  public Object WithRotateZ(Object main, Object originPoint, int angle){
    if (main instanceof Point && originPoint instanceof Point){
      return ((Point) main).rotateZ((Point) originPoint, angle);
    } else if (main instanceof Shape && originPoint instanceof Point) {
      return ((Shape) main).rotateZ((Point) originPoint, angle);
    } else if (main instanceof Path) {
      return ((Path) main).rotateZ((Point) originPoint, angle);
    } else {
      OnErrorOccurred("Error Type", "RotateZ");
      return null;
    }
  }
  */
  @SimpleFunction(description = "Rotates a point, shape, or path about the origin on the Z axis.")
  public Object RotateZ(Object main, Object originPoint, double angle) {
    if (main != null && originPoint instanceof Point) {
      try {
        Class<?> clazz = getType(main);
        Method method = clazz.getMethod("rotateZ", Point.class, double.class);
        return method.invoke(main, originPoint, angle);
      } catch (NoSuchMethodException e) {
        OnErrorOccurred(e.getMessage(), "RotateZ ~ Method");
      } catch (Exception e) {
        OnErrorOccurred("Error invoking method: " + e.getMessage(), "RotateZ");
      }
    } else {
      OnErrorOccurred("Invalid main or originPoint type for rotating Z", "RotateZ");
    }
    return null;
  }

}
