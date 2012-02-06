/*
 *  Copyright (c) 2010 readyState Software Ltd
 * 
 *  Modified 2012 by Lutz Reinhardt
 *
 *  This file is part of YAOHA.
 *
 *  YAOHA is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  YAOHA is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with YAOHA.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.yaoha;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.osmdroid.views.overlay.OverlayItem;

/**
 * A view representing a MapView marker information balloon.
 * 
 * @author Jeff Gilfelt
 *
 */
public class BalloonOverlayView<Item extends OverlayItem> extends FrameLayout {

	private LinearLayout layout;
	private TextView title;
	private TextView snippet;

	/**
	 * Create a new BalloonOverlayView.
	 * 
	 * @param context - The activity context.
	 * @param balloonBottomOffset - The bottom padding (in pixels) to be applied
	 * when rendering this view.
	 */
	public BalloonOverlayView(Context context, int balloonBottomOffset) {

		super(context);

		setPadding(10, 0, 10, balloonBottomOffset);
		layout = new LinearLayout(context);
		layout.setVisibility(VISIBLE);

		setupView(context, layout);

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.NO_GRAVITY;

		addView(layout, params);

	}

	/**
	 * Inflate and initialize the BalloonOverlayView UI. Override this method
	 * to provide a custom view/layout for the balloon. 
	 * 
	 * @param context - The activity context.
	 * @param parent - The root layout into which you must inflate your view.
	 */
	protected void setupView(Context context, final ViewGroup parent) {
		
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.balloon_overlay, parent);
		title = (TextView) v.findViewById(R.id.balloon_item_title);
		snippet = (TextView) v.findViewById(R.id.balloon_item_snippet);

		ImageView close = (ImageView) v.findViewById(R.id.balloon_close);
		close.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				parent.setVisibility(GONE);
			}
		});
		
	}
	
	/**
	 * Sets the view data from a given overlay item.
	 * 
	 * @param item - The overlay item containing the relevant view data. 
	 */
	public void setData(Item item) {
		layout.setVisibility(VISIBLE);
		setBalloonData(item, layout);
	}
	
	/**
	 * Sets the view data from a given overlay item. Override this method to create
	 * your own data/view mappings.
	 * 
	 * @param item - The overlay item containing the relevant view data.
	 * @param parent - The parent layout for this BalloonOverlayView.
	 */
	protected void setBalloonData(Item item, ViewGroup parent) {
		if (item.getTitle() != null) {
			title.setVisibility(VISIBLE);
			title.setText(item.getTitle());
		} else {
			title.setText("");
			title.setVisibility(INVISIBLE);
		}
		if (item.getSnippet() != null) {
			snippet.setVisibility(VISIBLE);
			snippet.setText(item.getSnippet());
		} else {
			snippet.setText("");
			snippet.setVisibility(INVISIBLE);
		}
	}

}
