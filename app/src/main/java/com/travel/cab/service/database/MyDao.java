package com.travel.cab.service.database;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface MyDao {
    @Insert
    void addUser(User user);

    @Query("select * from usersDetail")
    List<User> getUser();

    @Update
    void updateUserInfo(User user);

}
