# Add any ProGuard configurations specific to this
# extension here.

-keep public class me.aemo.isometricdrawing.IsometricDrawing {
    public *;
 }
-keeppackagenames gnu.kawa**, gnu.expr**

-optimizationpasses 4
-allowaccessmodification
-mergeinterfacesaggressively

-repackageclasses 'me/aemo/isometricdrawing/repack'
-flattenpackagehierarchy
-dontpreverify
