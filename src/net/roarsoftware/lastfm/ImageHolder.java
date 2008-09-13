package net.roarsoftware.lastfm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.roarsoftware.xml.DomElement;

/**
 * Abstract superclass for all beans containing image data.
 *
 * @author Janni Kovacs
 */
public abstract class ImageHolder {

	protected Map<ImageSize, String> imageUrls = new HashMap<ImageSize, String>();

	/**
	 * Returns the URL of the image in the specified size, or <code>null</code> if not available.
	 *
	 * @param size The preferred size
	 * @return an image URL
	 */
	public String getImageURL(ImageSize size) {
		return imageUrls.get(size);
	}

	protected static void loadImages(ImageHolder holder, DomElement element) {
		Collection<DomElement> images = element.getChildren("image");
		for (DomElement image : images) {
			String attribute = image.getAttribute("size");
			ImageSize size;
			if (attribute == null)
				size = ImageSize.MEDIUM; // workaround for image responses without size attr.
			else
				size = ImageSize.valueOf(attribute.toUpperCase());
			holder.imageUrls.put(size, image.getText());
		}
	}
}
