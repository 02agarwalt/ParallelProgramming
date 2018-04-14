import java.io.IOException;
import java.util.StringTokenizer;
import java.lang.Math;
import java.util.Arrays;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class FoF {
    public static class MyReducer extends Reducer<Text, NullWritable, Text, NullWritable> {
	private Text k1 = new Text();
	private Text k2 = new Text();
	private Text k3 = new Text();

	public void reduce(Text K, Iterable<NullWritable> V, Context c) throws InterruptedException, IOException {
	    int tot = 0;
	    for (NullWritable counter : V) {
		tot = tot + 1;
	    }
	    if (tot == 3) {
		String temp = K.toString();
		temp = temp.trim();
		String[] items = temp.split("\\s+");
		
		String temp1 = items[2] + " " + items[0] + " " + items[1];
		String temp2 = items[1] + " " + items[0] + " " + items[2];
		String temp3 = items[0] + " " + items[1] + " " + items[2];
		
		k1.set(temp3);
		k2.set(temp2);
		k3.set(temp1);

		c.write(k1, NullWritable.get());
		c.write(k2, NullWritable.get());
		c.write(k3, NullWritable.get());
	    }
	}
    }

    public static class MyMapper extends Mapper<Object, Text, Text, NullWritable> {
	private Text ks = new Text();

	public void map(Object K, Text V, Context c) throws IOException, InterruptedException {
	    String temp = V.toString();
	    temp = temp.trim();
	    String[] items = temp.split("\\s+");
	    int k1 = Integer.parseInt(items[0]);

	    for (int i = 1; i < items.length; i++) {
		for (int j = i + 1; j < items.length; j++) {
		    int k2 = Integer.parseInt(items[j]);
		    int k3 = Integer.parseInt(items[i]);
		    int[] temp = new int[3];
		    temp[0] = k1;
		    temp[1] = k2;
		    temp[2] = k3;
		    Arrays.sort(temp);
		    ks.set(temp[0] + " " + temp[1] + " " + temp[2]);
		    c.write(ks, NullWritable.get());
		}
	    }   
	}
    }

    public static void main(String[] args) throws Exception {
	Configuration config = new Configuration();
	Job job = Job.getInstance(config, "HW4");

	job.setJarByClass(FoF.class);
	job.setReducerClass(MyReducer.class);
	job.setMapperClass(MyMapper.class);
	
	job.setOutputKeyClass(Text.class);
	job.setOutputValueClass(NullWritable.class);

	FileOutputFormat.setOutputPath(job, new Path(args[1]));
	FileInputFormat.addInputPath(job, new Path(args[0]));

	if (job.waitForCompletion(true)) {
	    System.exit(0);
	} else {
	    System.exit(1);
	}
    }
}
