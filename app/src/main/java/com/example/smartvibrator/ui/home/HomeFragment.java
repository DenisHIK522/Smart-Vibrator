package com.example.smartvibrator.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartvibrator.MainActivity;
import com.example.smartvibrator.databinding.FragmentHomeBinding;
import com.example.smartvibrator.ui.Pattern;

public class HomeFragment extends Fragment
{
	private FragmentHomeBinding binding;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		binding = FragmentHomeBinding.inflate(inflater, container, false);
		View root = binding.getRoot();

		return root;
	}

	@Override
	public void onResume()
	{
		super.onResume();
		((MainActivity) getActivity()).update_grid();
		((MainActivity) getActivity()).update_chosen_pattern_information();
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		binding = null;
	}
}