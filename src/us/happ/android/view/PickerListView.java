package us.happ.android.view;



import us.happ.android.R;
import us.happ.android.activity.ComposeActivity;
import us.happ.android.utils.Media;
import us.happ.android.utils.SmoothInterpolator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.FrameLayout.LayoutParams;

public class PickerListView extends ListView {

	private int height;
	private int width;
	private Context mContext;
	private Paint paint;
	
	private static final int SELECTOR_BORDER_STROKE = 2; //dp
	private int selectorBorderStroke;
	
	private static final int LIST_ITEM_HEIGHT = 48; //dp
	private int childHeight;
	
	private int positionChosen;
	
	public PickerListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		paint = new Paint();
		selectorBorderStroke = (int) Media.pxFromDp(context, SELECTOR_BORDER_STROKE);
		childHeight = (int) Media.pxFromDp(context, LIST_ITEM_HEIGHT);
		
		setOnScrollListener(new OnScrollListener(){

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				
				int oldPosition = positionChosen;
				positionChosen = getSelectedPosition(firstVisibleItem);
				if (oldPosition != positionChosen)
					((ComposeActivity) mContext).setPicker(positionChosen);
				
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch(scrollState){
				case OnScrollListener.SCROLL_STATE_IDLE:
					smoothScroll();
		            break;
				}
			}
			
		});
		
		setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
				positionChosen = position-1;
				smoothScroll();
			}
		});
	}
	
	private void smoothScroll(){
		int pos = positionChosen + 1 - getFirstVisiblePosition();
		int offset = getChildAt(pos).getTop() - (height-childHeight)/2;
		if (offset != 0){
			SmoothScrollAnimation a = (new SmoothScrollAnimation());
			a.setInterpolator(new SmoothInterpolator());
			a.setDuration(500);
			a.setPosition(positionChosen + 1, offset);
			startAnimation(a);
		}
	}
	
	public void setChosen(int position){
		setSelectionFromTop(position + 1, (int) ((height-childHeight)/2)); // adjust for header
	}
	
	public int getChildHeight(){
		return childHeight;
	}
	
	public int getSelectedPosition(int firstVisibleId){
		int padding = 0;
		
		if (getChildAt(1) != null)
			padding = getChildAt(1).getTop();
			
		return firstVisibleId + (height/2 - padding)/childHeight;
	}
	
	public void setDimen(int width, int height){
		this.width = width;
		this.height = height;
		setHeaderFooter();
		setFadingEdgeLength((height-childHeight)/2);
	}
	
	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		
		if (height == 0) height = MeasureSpec.getSize(heightMeasureSpec);
		if (width == 0) width = MeasureSpec.getSize(widthMeasureSpec);
		
		setMeasuredDimension(width, height);
		
		View child;
		for(int i = 0; i < getChildCount(); i++){	
			child = getChildAt(i);
			
			measureChild(child, 
					MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
					MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY));
			
		}
	}
	
	private void setHeaderFooter(){
		View v = new View(mContext);
		v.setLayoutParams(new LayoutParams(width, (height - childHeight)/2));
		addHeaderView(v, null, false);
		v = new View(mContext);
		v.setLayoutParams(new LayoutParams(width, (height - childHeight)/2));
		addFooterView(v, null, false);
	}
	
	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		paint.setColor(mContext.getResources().getColor(R.color.happ_purple));
		paint.setStrokeWidth(selectorBorderStroke);
		canvas.drawLine(0, (height - childHeight)/2, width, (height - childHeight)/2, paint);
		canvas.drawLine(0, (height + childHeight)/2, width, (height + childHeight)/2, paint);
	}
	
	class SmoothScrollAnimation extends Animation {
		private int position;
		private int startPosition;
		
		public void setPosition(int position, int startPosition){
			this.position = position;
			this.startPosition = startPosition;
		}
		
		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			// TODO this keeps calling measure and layouts OPTIMIZE
			PickerListView.this.setSelectionFromTop(position, (int) ((height-childHeight)/2 + startPosition*(1 - interpolatedTime)));
	    }
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev){

		switch(ev.getAction()){
		case MotionEvent.ACTION_DOWN:
			clearAnimation();
			break;
		}
		return super.onTouchEvent(ev);
	}

	@Override
	protected void layoutChildren(){
		super.layoutChildren();
		
		for(int i = 0; i < getChildCount(); i++){
			View child = getChildAt(i);
			
			int height = child.getMeasuredHeight();
			int childTop = child.getTop();
			
			child.layout(0, childTop, width, childTop + height);
			
		}
	}


}
