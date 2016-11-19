/******************************************************************************* Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. ******************************************************************************/

package com.sidereal.lumm.architecture.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.sidereal.lumm.architecture.Lumm;
import com.sidereal.lumm.architecture.LummConfiguration;
import com.sidereal.lumm.architecture.LummModule;

/**
 * Class used for object serialization and saving in a path relative to the
 * preferences set by {@link LummConfiguration#preferExternalStorage} and
 * availability of the storage based on platform.
 * <p>
 * Instance is found in {@link Lumm#data}
 * 
 * @author Claudiu Bele
 */
public class AppData extends LummModule {

	// region external

	/**
	 * Type of storage. {@link AppData#storageType} is assigned a value in
	 * {@link AppData#DataSerializer(boolean)}
	 * 
	 * @see StorageType#External
	 * @see StorageType#Local
	 * @see StorageType#None
	 * @author Claudiu Bele
	 */
	enum StorageType {

		/**
		 * External is prioritised and available / Internal is prioritised and
		 * unavailable
		 * 
		 * @see StorageType#Local
		 */
		External,

		/**
		 * Internal is prioritised and available / External is prioritised and
		 * unavailable
		 * 
		 * @see StorageType#External
		 */
		Local,

		/** Neither external or local storage are available */
		None
	}

	public static final class ParticleSettings {

		public static final int NONE = 0;

		public static final int LOW = 1;

		public static final int MEDIUM = 2;

		public static final int HIGH = 3;

		public static final int MAX = 4;

		public static String toString(int no) {

			switch (no) {
			case 0:
				return Settings.PARTICLE_SETTINGS_NONE;
			case 1:
				return Settings.PARTICLE_SETTINGS_LOW;
			case 2:
				return Settings.PARTICLE_SETTINGS_MEDIUM;
			case 3:
				return Settings.PARTICLE_SETTINGS_HIGH;
			case 4:
				return Settings.PARTICLE_SETTINGS_MAX;
			default:
				return Settings.PARTICLE_SETTINGS_NONE;
			}
		}
	}

	public static final class Settings {

		/**
		 * Specifies the setting number for the particle settings.
		 * <p>
		 * Value is of type {@link IntWrapper}.
		 */
		public static final String PARTICLE_SETTINGS = "Particle settings";

		/**
		 * Whether or not to use linear filtering for the loaded textures
		 * <p>
		 * Set to false by default in {@link AppData#onCreate()}, and retrieved
		 * or added to the settings file based on whether or not it exists.
		 * <p>
		 * Value is of type {@link BooleanWrapper}.
		 */
		public static final String LINEAR_FILTERING = "Linear filtering";

		/**
		 * Specifies whether or not to use shaders. Set to true by default.
		 * <p>
		 * Value is of type {@link BooleanWrapper}.
		 */
		public static final String USE_SHADERS = "Use shaders";

		/**
		 * Music volume modifier, from 0 to 1.
		 * <p>
		 * Value is of type {@link FloatWrapper}.
		 */
		public static final String MUSIC_VOLUME = "Music volume";

		/**
		 * Sound volume modifier, from 0 to 1.
		 * <p>
		 * Value is of type {@link FloatWrapper}.
		 */
		public static final String SOUND_VOLUME = "Sound volume";

		/**
		 * Whether to run multithreaded or not.
		 * <p>
		 * Value is of type {@link BooleanWrapper}.
		 */
		public static final String MULTI_THREADED = "Multi threaded";

		public static final String PARTICLE_SETTINGS_NONE = "NONE";

		public static final String PARTICLE_SETTINGS_LOW = "LOW";

		public static final String PARTICLE_SETTINGS_MEDIUM = "MEDIUM";

		public static final String PARTICLE_SETTINGS_HIGH = "HIGH";

		public static final String PARTICLE_SETTINGS_MAX = "MAX";

	}

	// endregion

	// region fields

	public static final String GRAPHICS_SETTINGS_PATH = "GameData/Graphics";

	public static final String AUDIO_SETTINGS_PATH = "GameData/Audio";

	public static final String LOAD_ON_STARTUP_PATH = "GameData/LoadOnStartup";

	/** The graphical preferences, used for retrieval from files */
	public HashMap<String, Object> graphicSettings;

	/**
	 * Audio preferences of the app, containing audio channel volume settings
	 */
	public HashMap<String, Object> audioSettings;

	/**
	 * List containing whether files have to be loaded on startup. When using
	 * {@link #saveData(String, Object, boolean)}
	 */
	private HashMap<String, Boolean> loadOnStartup;

	/**
	 * Whether to prioritise external storage when saving data or not, if the
	 * device allows.
	 */
	private boolean prioritiseExternalStorage;

	/** The root path to all data related to the app. */
	private String rootDataPath;

	/** The {@link File} object created at the {@link #rootDataPath} path. */
	private FileHandle rootDataFolder;

	/**
	 * Serialized data with the {@link String} key resembling the path and the
	 * value being the deserialized data which can be casted as the class it was
	 * initially passed for serialization.
	 */
	private ObjectMap<String, Object> data;

	/** Paths to all of the files. Created in {@link #onCreate()} */
	private ArrayList<String> filePaths;

	/**
	 * The type of storage. Set in {@link #DataSerializer(boolean)} which sets
	 * it based on {@link LummConfiguration#preferExternalStorage} and
	 * {@link Gdx#files#isExternalStorageAvailable()}
	 */
	private StorageType storageType;

	// endregion fields

	// region constructors

	public AppData(LummConfiguration cfg) {

		super(cfg);
		setUpdateFrequency(-1);
		this.rootDataPath = cfg.rootDataPath;
		this.prioritiseExternalStorage = cfg.prioritiseExternalStorage;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onCreate() {

		Lumm.debug.log("Is local storage available? " + Gdx.files.isLocalStorageAvailable(), null);
		Lumm.debug.log("Is external storage available? " + Gdx.files.isExternalStorageAvailable(), null);

		// region handle root path
		if (Gdx.files.isExternalStorageAvailable() == Gdx.files.isLocalStorageAvailable() == false) {
			storageType = StorageType.None;
			Lumm.debug.logDebug("Device does not allow internal or external storage", null);
			return;
		}

		// check if external storage is available.
		if (prioritiseExternalStorage) {
			if (Gdx.files.isExternalStorageAvailable())
				storageType = StorageType.External;
			else if (Gdx.files.isLocalStorageAvailable())
				storageType = StorageType.Local;
		} else {
			// prioritising local storage
			if (Gdx.files.isLocalStorageAvailable())
				storageType = StorageType.Local;
			else if (Gdx.files.isExternalStorageAvailable())
				storageType = StorageType.External;
		}

		if (storageType == StorageType.Local)
			Lumm.debug.log("Storage type set to local", null);
		else
			Lumm.debug.log("Storage type set to external", null);

		rootDataPath = rootDataPath.replace('*', ' ').replace('?', ' ');
		if (rootDataPath.charAt(rootDataPath.length() - 1) != '/') {
			rootDataPath += "/";
			Lumm.debug.log("Storage root folder set to \"" + rootDataPath + "\"", null);
		}
		// endregion

		filePaths = new ArrayList<String>();
		data = new ObjectMap<String, Object>();

		// creates folders
		getFileHandle(rootDataPath.substring(0, rootDataPath.lastIndexOf("/"))).mkdirs();

		/**
		 * Deserialises the data from all of the files found in the Data folder(
		 * including subfolders of Data). Gets called only if the storageType is
		 * not null so no need to check
		 */
		getDataFromFolder(getRootDataFolder(), "");

		if (exists(LOAD_ON_STARTUP_PATH)) {
			loadOnStartup = (HashMap<String, Boolean>) load(LOAD_ON_STARTUP_PATH);
		} else {
			loadOnStartup = new HashMap<String, Boolean>();
			loadOnStartup.put(GRAPHICS_SETTINGS_PATH, new Boolean(true));
			loadOnStartup.put(AUDIO_SETTINGS_PATH, new Boolean(true));
			save(LOAD_ON_STARTUP_PATH, loadOnStartup, true, true);

		}

		for (java.util.Map.Entry<String, Boolean> entry : loadOnStartup.entrySet()) {
			if (exists(entry.getKey()) && entry.getValue())
				load(entry.getKey());
		}

		// handle retrieving or setting the preferences
		if (contains(GRAPHICS_SETTINGS_PATH)) {
			graphicSettings = (HashMap<String, Object>) get(GRAPHICS_SETTINGS_PATH);
		} else {
			graphicSettings = new HashMap<String, Object>();
			graphicSettings.put(Settings.PARTICLE_SETTINGS, new Integer(ParticleSettings.MAX));
			graphicSettings.put(Settings.LINEAR_FILTERING, new Boolean(false));
			graphicSettings.put(Settings.USE_SHADERS, new Boolean(true));
			graphicSettings.put(Settings.MULTI_THREADED, new Boolean(false));
			save(GRAPHICS_SETTINGS_PATH, graphicSettings, true, true);

		}
		
		if (contains(AUDIO_SETTINGS_PATH)) {
			audioSettings = (HashMap<String, Object>) get(AUDIO_SETTINGS_PATH);
		} else {
			audioSettings = new HashMap<String, Object>();
			audioSettings.put(Audio.MASTER_VOLUME_KEY, new Float(1));
			audioSettings.put(Audio.MUSIC_VOLUME_KEY, new Float(1));
			audioSettings.put(Audio.VOICE_VOLUME_KEY, new Float(1));
			audioSettings.put(Audio.EFFECTS_VOLUME_KEY, new Float(1));
			audioSettings.put(Audio.ENVIRONMENT_VOLUME_KEY, new Float(1));
			audioSettings.put(Audio.UI_VOLUME_KEY, new Float(1));

			save(AUDIO_SETTINGS_PATH, audioSettings, true, true);
		}

	}

	@Override
	public void onUpdate() {

	}


	// endregion constructor

	// region methods

	// region settings

	/**
	 * Retrieves a setting from {@link #graphicSettings}. Use {@link Settings}
	 * variables as a parameter for easy access.
	 * 
	 * @param settingName
	 * @return
	 */
	public Object getSettings(String settingName) {

		if (graphicSettings != null) {
			if (graphicSettings.containsKey(settingName)) {
				return graphicSettings.get(settingName);
			}
		}
		return null;
	}

	public void updateSettings() {

		if (graphicSettings != null) {
			save(GRAPHICS_SETTINGS_PATH, graphicSettings, true, true);
		}
	}

	// endregion settings

	// region API

	// region checks

	/**
	 * Returns whether or not data with the given parameter has been loaded.
	 * 
	 * @param dataName
	 *            The path excluding the root folder, it is the same one used
	 *            for saving data.
	 * @return whether or not the parameter can be found as a key.
	 */
	public boolean contains(String dataName) {

		return data.containsKey(dataName);
	}

	public boolean exists(String dataName) {

		for (int i = 0; i < filePaths.size(); i++) {
			if (filePaths.get(i).equals(dataName))
				return true;
		}
		return false;
	}

	// endregion checks

	// region save
	/**
	 * Saves the object as a file at root/dataName by serialising it
	 * 
	 * @param dataName
	 *            The name of the file path relative to the Data Folder, as well
	 *            as the key used for {@link #data}
	 * @param obj
	 *            The value of the object. WIll be the data in the file, as well
	 *            as the value of the entry that the object takes in
	 *            {@link #data}
	 *            <p>
	 *            Passing a null object as a parameter will only make the
	 *            folders
	 */
	public final void save(String dataName, Object obj, boolean loadOnStartup, boolean storeInMemory) {

		if (storageType.equals(StorageType.None))
			return;

		String path = rootDataPath + "/" + dataName;
		getFileHandle(path.substring(0, path.lastIndexOf("/"))).mkdirs();

		if (obj == null)
			return;

		// we are done with creating nested files.
		try {
			FileHandle file = getFileHandle(rootDataPath + dataName + ".dd");
			file.writeBytes(serialize(obj), false);
			Lumm.debug.log("Saved object to file at path \"" + rootDataPath + dataName + ".dd" + "\"", null);

			if (storeInMemory) {
				data.put(dataName, obj);
			}

			// not in the list of files in the system, add it there.
			if (!exists(dataName))
				filePaths.add(dataName);

			// load on startup won't update itself with its' own startup
			// information, we return.
			if (dataName.equals(LOAD_ON_STARTUP_PATH))
				return;

			if (!this.loadOnStartup.containsKey(dataName) || this.loadOnStartup.get(dataName) != loadOnStartup) {
				this.loadOnStartup.put(dataName, new Boolean(loadOnStartup));
				save(LOAD_ON_STARTUP_PATH, this.loadOnStartup, false, true);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Saves all data in their filePath. */
	public final void saveAll() {

		for (Entry<String, Object> entry : data.entries()) {
			save(entry.key, entry.value, loadOnStartup.get(entry.key), false);
		}
	}

	// endregion save

	// region delete
	/**
	 * Removes a file from the map if it the key can be found, and removes the
	 * file from the disk.
	 * <p>
	 * {@link #filePaths}, {@link #loadOnStartup} and {@link #data} will be
	 * updated to reflect the changes.
	 * 
	 * @param dataName
	 *            The key to the data
	 */
	public final void delete(String dataName) {

		if (storageType.equals(StorageType.None))
			return;

		Lumm.debug.log("Deleting file from disk at location: " + dataName, null);
		if (contains(dataName)) {
			FileHandle file = getFileHandle(rootDataPath + dataName + ".dd");
			if (file.exists()) {
				file.delete();
			}
			data.remove(dataName);
			filePaths.remove(dataName);
			loadOnStartup.remove(dataName);
			save(LOAD_ON_STARTUP_PATH, this.loadOnStartup, false, true);
		}
	}

	/**
	 * Deletes all of the files ( excluding {@link #graphicSettings} and
	 * {@link #filePaths} ) from the disk.
	 * <p>
	 * {@link #filePaths}, {@link #loadOnStartup} and {@link #data} will be
	 * updated to reflect the changes.
	 */
	public final void deleteAll() {
		for (int i = 0; i < filePaths.size(); i++) {
			// keep special files
			if (filePaths.get(i).equals(GRAPHICS_SETTINGS_PATH) || filePaths.get(i).equals(LOAD_ON_STARTUP_PATH))
				continue;

			// remove ordinary files
			getFileHandle(rootDataPath + "/" + filePaths.get(i)).delete();

			data.clear();
			loadOnStartup.clear();

			data.put(GRAPHICS_SETTINGS_PATH, graphicSettings);
			data.clear();
			filePaths.clear();

			save(LOAD_ON_STARTUP_PATH, loadOnStartup, true, true);
			save(GRAPHICS_SETTINGS_PATH, graphicSettings, true, true);

		}
	}

	// endregion delete

	// region get

	/**
	 * Returns the entry in the {@link #data} with the specificied key parameter
	 * 
	 * @param dataName
	 *            The key to be used for retrieval of the value tied to it from
	 *            {@link #data}
	 * @return The value for the specified key or null, based on whether or not
	 *         it exists in the Dictionary
	 */
	public final Object get(String dataName) {

		return (data.containsKey(dataName)) ? data.get(dataName) : null;
	}

	public final ArrayList<Object> getAllFromFolder(String folderName) {

		ArrayList<Object> objects = new ArrayList<Object>();

		for (Entry<String, Object> entry : data.entries()) {
			if (entry.key.startsWith(folderName)) {
				objects.add(entry.value);
			}
		}
		return objects;
	}

	// endregion get

	// region loadData

	/**
	 * Loads a file in memory. This method has to be call in order to load data
	 * that had the <code>loadOnstartup</code> parameter passed in
	 * {@link #saveData(String, Object, boolean)} set to false, otherwise the
	 * asset will already be loaded at startup.
	 * <p>
	 * If the data is already in memory, the request is ignored.
	 * 
	 * @param filepath
	 *            the path to the file to load. It is the path relative to the
	 *            root file path.
	 */
	public Object load(String filepath) {

		// already loaded, return
		if (contains(filepath))
			return null;

		if (exists(filepath)) {
			Object obj;
			try {

				obj = deserialize(getFileHandle(rootDataPath + "/" + filepath + ".dd").readBytes());
				data.put(filepath, obj);
				Lumm.debug.log("Deserialized file at path \"" + filepath + "\"", null);
				return obj;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * Uncloads a file from memory. For an asset to be loaded, upon saving the
	 * data using {@link #saveData(String, Object, boolean)}, the
	 * <code>loadOnStartup</code> parameter has to be true, or
	 * {@link #load(String)} has to be called.
	 * <p>
	 * If the data is not in memory anymore, the request is ignored
	 * 
	 * @param filepath
	 *            the path to the file to unload. It is the path relative to the
	 *            root file path.
	 */
	public final void unload(String filepath) {

		// not loaded, return;
		if (!contains(filepath))
			return;

		data.remove(filepath);
	}

	/**
	 * Releases the references to files. File paths will not be removed, so you
	 * would still know where all the files are using {@link #exists(String)}.
	 * <p>
	 * . For an asset to be loaded, upon saving the data using
	 * {@link #saveData(String, Object, boolean)}, the
	 * <code>loadOnStartup</code> parameter has to be true, or
	 * {@link #load(String)} has to be called.
	 * <p>
	 * If the data is not in memory anymore, the request is ignored
	 */
	public final void unloadAll() {
		data.clear();
	}

	// endregion loadData

	// endregion API

	// region internal

	private byte[] serialize(Object obj) throws IOException {

		ByteArrayOutputStream b = new ByteArrayOutputStream();
		ObjectOutputStream o = new ObjectOutputStream(b);
		o.writeObject(obj);
		return b.toByteArray();
	}

	private Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {

		ByteArrayInputStream b = new ByteArrayInputStream(bytes);
		ObjectInputStream o = new ObjectInputStream(b);
		return o.readObject();
	}

	private FileHandle getFileHandle(String path) {

		if (storageType.equals(StorageType.None))
			return null;

		if (storageType.equals(StorageType.External))
			return Gdx.files.external(path);
		return Gdx.files.local(path);
	}

	/**
	 * Returns the Data folder.
	 * <p>
	 * It will be created if it doesn't exists.
	 * 
	 * @return The Data folder
	 */
	private final FileHandle getRootDataFolder() {

		FileHandle file = getFileHandle(rootDataPath.substring(0, rootDataPath.length() - 1));
		// there is no root data folder
		if (!file.exists() || !file.isDirectory()) {

			// making files if they don't exist
			if (!file.exists() && rootDataFolder != null) {
				file.mkdirs();
				saveAll();
			} else {
				file.mkdirs();
			}
			rootDataFolder = file;
		}
		return file;

	}

	/**
	 * Method for recursively accessing files.
	 * <p>
	 * Goes over all the files in a folder( keeping in mind the name of the path
	 * so far from Data until the current file) if it is a folder, if not, tries
	 * to deserialise it and add it to {@link #data} with the key being the path
	 * from Data to the file ( excluding Data/) and the value being the
	 * deserialised object.
	 * <p>
	 * No need to check for storageType to be {@link StorageType#None} as this
	 * is called from all files.
	 * 
	 * @param folder
	 *            current file to search through
	 * @param currentPathString
	 *            the path so far.
	 */
	private void getDataFromFolder(FileHandle folder, String currentPathString) {

		// get a list of all files
		FileHandle[] filesInFolder = folder.list();
		if (filesInFolder == null)
			return;

		for (int i = 0; i < filesInFolder.length; i++) {
			// // can't read, don't bother handling it.
			// if (!filesInFolder[i].)
			// continue;

			if (filesInFolder[i].isDirectory()) {
				getDataFromFolder(filesInFolder[i], currentPathString + filesInFolder[i].name() + "/");
			} else
			// object is file
			{
				// remove the .dd from it
				String filepath = currentPathString
						+ filesInFolder[i].name().substring(0, filesInFolder[i].name().length() - 3);
				Lumm.debug.log("Found file at path \"" + filepath + "\"", null);
				if (!exists(filepath))
					filePaths.add(filepath);
			}
		}
	}

	@Override
	public List<Class<? extends LummModule>> getDependencies() {
		List<Class<? extends LummModule>> modules = new ArrayList<Class<? extends LummModule>>();
		modules.add(Debug.class);
		return modules;
	}

	
	// endregion internal

	// endregion methods

}
