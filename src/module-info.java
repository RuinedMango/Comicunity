module Comicunity {
	requires javafx.controls;
	requires javafx.graphics;
	requires java.desktop;
	requires javafx.base;
	requires org.apache.commons.compress;
	requires org.tukaani.xz;
	
	opens application to javafx.graphics, javafx.fxml;
}
