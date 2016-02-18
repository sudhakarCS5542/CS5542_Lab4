package com.example.sudhakareddy.imagesearch;

    import android.support.v4.util.SimpleArrayMap;
    import android.view.View;
    import android.view.ViewGroup;
    import android.view.ViewGroup.LayoutParams;
    import android.widget.BaseAdapter;
    import android.widget.GridView;
    import android.widget.ImageView;

    public class ImageAdapter extends BaseAdapter {

        private MainActivity context;
        private int contiguousResults;
        private SimpleArrayMap<Integer, ImageResult> results;

        public ImageAdapter(MainActivity context) {
            this.contiguousResults = 0;
            this.results = new SimpleArrayMap<Integer, ImageResult>();
            this.context = context;
        }

        public int getCount() {
            return Math.min(contiguousResults, context.MAX_RESULTS);
        }

        public Object getItem(int position) {
            return results.valueAt(position);
        }

        public long getItemId(int position) {
            //not implemented, not needed for this application
            return 0;
        }

        public void addResult(ImageResult result){
            this.results.put(result.result, result);

            int i = contiguousResults;
            while(results.get(i) != null){
                i++;
            }

            if(i!=contiguousResults){
                contiguousResults = i;
                notifyDataSetChanged();
            }
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ImageView imageView;

            if (convertView == null) {
                imageView = new ImageView(context);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setLayoutParams(new GridView.LayoutParams(LayoutParams.MATCH_PARENT, 300));
            } else {
                imageView = (ImageView) convertView;
                imageView.setImageResource(android.R.color.transparent);
            }

            String imageUrl = results.get(position).imageUrl;
            imageView.setImageBitmap(context.imageCache.get(imageUrl));

            return imageView;
        }
    }
}
