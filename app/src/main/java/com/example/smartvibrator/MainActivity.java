package com.example.smartvibrator;

import android.Manifest;
import android.app.Service;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartvibrator.ui.Icon;
import com.example.smartvibrator.ui.MyAdapter;
import com.example.smartvibrator.ui.Pattern;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.smartvibrator.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


/* ========================================================== */
/* 由于不是大工程，懒得分模块写了，所有业务逻辑都堆在这一个代码文件里 */
public class MainActivity extends AppCompatActivity
{
	private Pattern chosen_pattern;
	private GridView grid_photo;
	private String record_path;
	private ArrayList<Pattern> pattern_list;
	private ActivityMainBinding binding;
	private BaseAdapter mAdapter = null;
	private ArrayList<Icon> mData = null;
	private Random random_generator;
	private Vibrator vibrator;
	private final int[] icon_list = {
			R.mipmap.iv_icon_1, R.mipmap.iv_icon_2, R.mipmap.iv_icon_3, R.mipmap.iv_icon_4, R.mipmap.iv_icon_5,
			R.mipmap.iv_icon_6, R.mipmap.iv_icon_7, R.mipmap.iv_icon_8, R.mipmap.map, R.mipmap.qqmail,
			R.mipmap.typora, R.mipmap.ubuntu, R.mipmap.visio, R.mipmap.vue, R.mipmap.webstrom
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.chosen_pattern = null;
		this.pattern_list = new ArrayList<>();
		this.mData = new ArrayList<Icon>();
		this.random_generator = new Random(System.nanoTime());
		this.vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);

		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		BottomNavigationView navView = findViewById(R.id.nav_view);
		// Passing each menu ID as a set of Ids because each
		// menu should be considered as top level destinations.
		AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications).build();
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
		NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
		NavigationUI.setupWithNavController(binding.navView, navController);

		if (!grant_permission())
		{
			Toast.makeText(getApplicationContext(), "We need permissions.", Toast.LENGTH_LONG).show();
		}

		if (chosen_pattern != null)
		{
			update_chosen_pattern_information();
		}

		this.record_path = check_record();

		if (this.record_path != null)
		{
			load_patterns();
			update_grid();
		}
		else
		{
			Toast.makeText(getApplicationContext(), "Can not read record file.", Toast.LENGTH_LONG).show();
		}
	}

	public void update_grid()
	{
		grid_photo = (GridView) findViewById(R.id.grid);
		mData.clear();

		for (Pattern pattern : pattern_list)
		{
			mData.add(new Icon(icon_list[pattern.get_icon_id()], pattern.get_name()));
		}

		mAdapter = new MyAdapter<Icon>(mData, R.drawable.gridview_item)
		{
			@Override
			public void bindView(ViewHolder holder, Icon obj)
			{
				holder.setImageResource(R.id.img_icon, obj.getiId());
				holder.setText(R.id.txt_icon, obj.getiName());
			}
		};

		grid_photo.setAdapter(mAdapter);

		grid_photo.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Toast.makeText(getApplicationContext(), "You chose " + pattern_list.get(position).get_name(), Toast.LENGTH_SHORT).show();
				chosen_pattern = pattern_list.get(position);
				update_chosen_pattern_information();
			}
		});
	}

	public void load_patterns()
	{
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(this.record_path));
			String file_content = "";
			StringBuffer output;

			while (reader.ready())
			{
				output = new StringBuffer(reader.readLine());
				file_content += output + "\n";
			}

			if (file_content.length() < 5)
			{
				pattern_list.clear();
				chosen_pattern = null;
				return;
			}

			file_content = file_content.substring(0, file_content.length() - 1);

			if (file_content.length() < 10)
			{
				return;
			}

			String[] temp_list = file_content.split("\n\n");

			for (String element : temp_list)
			{
				String[] attributes = element.split("\n");
				String name = attributes[1], timing_list = attributes[2], amplitude_list = attributes[3], repeat_times = attributes[4];
				int icon_id = Integer.parseInt(attributes[0]);

				pattern_list.add(new Pattern(name, timing_list, amplitude_list, repeat_times, icon_id));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(100);
		}
	}

	public void delete_pattern(View view)
	{
		try
		{
			if (this.chosen_pattern != null && !this.pattern_list.isEmpty())
			{
				this.pattern_list.remove(this.chosen_pattern);

				if (save_current_pattern_list())
				{
					this.chosen_pattern = null;
					save_current_pattern_list();
					update_grid();
				}
			}
			else
			{
				Toast.makeText(getApplicationContext(), "Please choose a pattern.", Toast.LENGTH_LONG).show();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "Deletion failed.", Toast.LENGTH_LONG).show();
		}
	}

	public void create_pattern(View view)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
		View Tittleview = getLayoutInflater().inflate(R.layout.modal_title, null);
		ImageView img2 = (ImageView) Tittleview.findViewById(R.id.img2);
		TextView textView = (TextView) Tittleview.findViewById(R.id.tv2);

		textView.setText("Create Your Own Pattern");
		img2.setImageResource(R.mipmap.iv_icon_1);
		builder.setCustomTitle(Tittleview);

		View contentView = getLayoutInflater().inflate(R.layout.modal_content, null);
		EditText pattern_name = (EditText) contentView.findViewById(R.id.create_pattern_name);
		EditText pattern_timing = (EditText) contentView.findViewById(R.id.create_pattern_timing);
		EditText pattern_amplitude = (EditText) contentView.findViewById(R.id.create_pattern_amplitude);
		EditText pattern_repeat = (EditText) contentView.findViewById(R.id.create_pattern_repeat);

		builder.setView(contentView);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				/* update pattern list */
				try
				{
					int temp_icon_index = random_generator.nextInt(16);
					Pattern new_pattern = new Pattern(
							pattern_name.getText().toString(),
							pattern_timing.getText().toString(),
							pattern_amplitude.getText().toString(),
							pattern_repeat.getText().toString(),
							temp_icon_index
					);
					pattern_list.add(new_pattern);

					update_grid();
					save_current_pattern_list();

					Toast.makeText(getApplicationContext(), "creating pattern succeed.", Toast.LENGTH_LONG).show();
				}
				catch (Exception e)
				{
					e.printStackTrace();
					Toast.makeText(getApplicationContext(), "creating pattern failed.", Toast.LENGTH_LONG).show();
				}
			}
		}).setNegativeButton("Cancel", null).create().show();
	}

	public String check_record()
	{
		try
		{
			File record = new File(getExternalCacheDir(), "record.txt");
			String record_path = record.getAbsolutePath();

			if (!record.exists())
			{
				record.createNewFile();
			}

			return record_path;
		}
		catch (Exception e)
		{
			e.printStackTrace();

			return null;
		}
	}

	public boolean save_current_pattern_list()
	{
		String output = "";

		if (!pattern_list.isEmpty())
		{
			for (int i = 0; i < pattern_list.size(); i++)
			{
				Pattern temp_pattern = pattern_list.get(i);

				if (i == 0)
				{
					output += temp_pattern.get_record();
				}
				else
				{
					output += "\n\n" + temp_pattern.get_record();
				}
			}
		}

		try
		{
			File output_file = new File(record_path);
			FileWriter writer = new FileWriter(output_file, false);

			writer.write(output);
			writer.close();

			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "Write record failed.", Toast.LENGTH_LONG).show();

			return false;
		}
	}

	/* for pattern information displayed on the home page */
	public void update_chosen_pattern_information()
	{
		if (chosen_pattern != null)
		{
			((TextView) findViewById(R.id.pattern_name)).setText(chosen_pattern.get_name());

			String temp = "";

			for (Long element : chosen_pattern.get_timing_list())
			{
				temp += element.toString() + ",";
			}

			temp = temp.substring(0, temp.length() - 1);
			((TextView) findViewById(R.id.pattern_timing)).setText(temp);

			temp = "";

			for (Integer element : chosen_pattern.get_amplitude_list())
			{
				temp += element.toString() + ",";
			}

			temp = temp.substring(0, temp.length() - 1);
			((TextView) findViewById(R.id.pattern_amplitude)).setText(temp);

			((TextView) findViewById(R.id.pattern_repeat)).setText(Integer.toString(chosen_pattern.get_repeat_times()));
		}

	}

	/* for pattern information displayed on the vibrator page */
	public void update_pattern_information()
	{
		if (chosen_pattern != null)
		{
			((TextView) findViewById(R.id.chosen_pattern_name)).setText(chosen_pattern.get_name());

			String temp = "";

			for (Long element : chosen_pattern.get_timing_list())
			{
				temp += element.toString() + ",";
			}

			temp = temp.substring(0, temp.length() - 1);
			((TextView) findViewById(R.id.chosen_pattern_timing)).setText(temp);

			temp = "";

			for (Integer element : chosen_pattern.get_amplitude_list())
			{
				temp += element.toString() + ",";
			}

			temp = temp.substring(0, temp.length() - 1);
			((TextView) findViewById(R.id.chosen_pattern_amplitude)).setText(temp);

			((TextView) findViewById(R.id.chosen_pattern_repeat)).setText(Integer.toString(chosen_pattern.get_repeat_times()));
		}

	}

	/* check if permissions are granted */
	private boolean check_permission(String[] permissions)
	{
		for (String permission : permissions)
		{
			if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
			{
				return false;
			}
		}

		return true;
	}

	/* asking for permission */
	public boolean grant_permission()
	{
		String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.VIBRATE};

		try
		{
			if (!check_permission(permissions))
			{
				ActivityCompat.requestPermissions(this, permissions, 10000);
			}

			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();

			return false;
		}
	}

	public void start_vibration(View view)
	{
		if (this.chosen_pattern == null)
		{
			Toast.makeText(getApplicationContext(), "Please choose a pattern first", Toast.LENGTH_LONG).show();
		}
		else
		{
			long[] times = this.chosen_pattern.get_timing_list().stream().mapToLong(t -> t.longValue()).toArray();
			int[] amplitude = this.chosen_pattern.get_amplitude_list().stream().mapToInt(t -> t.intValue()).toArray();
			vibrator.vibrate(VibrationEffect.createWaveform(times, amplitude, this.chosen_pattern.get_repeat_times()));
		}
	}

	public void stop_vibration(View view)
	{
		vibrator.cancel();
	}
}