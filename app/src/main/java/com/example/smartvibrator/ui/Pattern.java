package com.example.smartvibrator.ui;

import java.util.ArrayList;

public class Pattern
{
	private String name;
	private ArrayList<Long> timing_list;
	private ArrayList<Integer> amplitude_list;
	private int repeat_times, icon_id;

	public Pattern(String name, String timing, String amplitude, String repeat_times, int icon_id)
	{
		this.name = name;
		this.timing_list = new ArrayList<>();
		this.amplitude_list = new ArrayList<>();
		this.icon_id = icon_id;
		String[] temp = timing.split(",");

		for (String element : temp)
		{
			timing_list.add(Long.parseLong(element));
		}

		temp = amplitude.split(",");

		for (String element : temp)
		{
			amplitude_list.add(Integer.parseInt(element));
		}

		this.repeat_times = Integer.parseInt(repeat_times);
	}

	public String get_record()
	{
		String result = Integer.toString(this.icon_id) + "\n" + this.name + "\n";

		for (Long element : this.timing_list)
		{
			result += element.toString() + ",";
		}

		result = result.substring(0, result.length() - 1);
		result += "\n";

		for (Integer element : this.amplitude_list)
		{
			result += element.toString() + ",";
		}

		result = result.substring(0, result.length() - 1);
		result += "\n";

		result += Integer.toString(this.repeat_times);

		return result;
	}

	public ArrayList<Long> get_timing_list()
	{
		return this.timing_list;
	}

	public ArrayList<Integer> get_amplitude_list()
	{
		return this.amplitude_list;
	}

	public String get_name()
	{
		return this.name;
	}

	public int get_repeat_times()
	{
		return this.repeat_times;
	}

	public int get_icon_id()
	{
		return this.icon_id;
	}
}
