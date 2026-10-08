import qpal.model.RouteDropPoints;
public class RouteDropPointsTest {
 public static void main(String[] args) {
  if(!RouteDropPoints.route("Dasmariñas").equals(java.util.List.of("PITX","Bacoor","Imus","Dasmariñas")))throw new AssertionError();
  if(RouteDropPoints.options("Naic").size()!=2)throw new AssertionError("Duplicate options");
  if(!RouteDropPoints.options("GMA").contains("Salawag"))throw new AssertionError("Alias");
  if(RouteDropPoints.valid("PITX","Naic","Imus"))throw new AssertionError("Invalid stop");
  if(RouteDropPoints.valid("Elsewhere","Naic","Tanza"))throw new AssertionError("Origin");
  if(!RouteDropPoints.options("Unknown").equals(java.util.List.of("Unknown")))throw new AssertionError();
  System.out.println("PASS: mappings, duplicate stops, aliases, invalid selections and fallback");
  var full = new java.math.BigDecimal("50");
  if(!RouteDropPoints.fare("Amadeo","Silang",full).equals(new java.math.BigDecimal("30.00")))throw new AssertionError("First stop");
  if(!RouteDropPoints.fare("Amadeo","Tagaytay",full).equals(new java.math.BigDecimal("40.00")))throw new AssertionError("Second stop");
  if(!RouteDropPoints.fare("Amadeo","Amadeo",full).equals(new java.math.BigDecimal("50.00")))throw new AssertionError("Final stop");
  if(!RouteDropPoints.fare("Naic","Naic",full).equals(new java.math.BigDecimal("50.00")))throw new AssertionError("Repeated final stop");
  if(!RouteDropPoints.fare("Amadeo","Silang",new java.math.BigDecimal("50.01")).equals(new java.math.BigDecimal("30.01")))throw new AssertionError("Rounding");
  try { RouteDropPoints.fare("Amadeo","PITX",full); throw new AssertionError("Origin selectable"); }
  catch(IllegalArgumentException expected) { }
  System.out.println("PASS: fare percentages, final stop, rounding and origin rejection");
 }
}
