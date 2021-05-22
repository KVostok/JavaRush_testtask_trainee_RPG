package com.game.service;

import com.game.entity.*;
import com.game.exceptions.*;
import com.game.repository.PlayerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.Calendar;
import java.util.Date;

@Service
public class PlayerService{

    private static final int MAX_LENGTH_NAME = 12;
    private static final int MAX_LENGTH_TITLE = 30;
    private static final int MAX_SIZE_EXPERIENCE = 10000000;
    private static final long MIN_BIRTHDAY = 2000L;
    private static final long MAX_BIRTHDAY = 3000L;
    private PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public Integer calculateCurrentLevel(Integer experience) {
        return (((int)Math.sqrt(2500+200*experience))-50)/100;
    }

    private Integer calculateExperienceUntilNextLevel(Integer experience, Integer level) {
        return 50*(level+1)*(level+2)-experience;
    }

    public Page<Player> findAllPlayers(Specification<Player> specification, Pageable pageable) {
        return playerRepository.findAll(specification, pageable);
    }

    public Long getCountPlayers(Specification<Player> specification) {
        return playerRepository.count(specification);
    }

    public Player createPlayer(Player player) {
        checkName(player.getName());
        checkTitle(player.getTitle());
        checkRace(player.getRace());
        checkProfession(player.getProfession());
        checkBirthday(player.getBirthday());
        checkExperience(player.getExperience());
        if(player.getBanned() == null) player.setBanned(false);
        player.setLevel(calculateCurrentLevel(player.getExperience()));
        player.setUntilNextLevel(calculateExperienceUntilNextLevel(player.getExperience(), player.getLevel()));
        return playerRepository.saveAndFlush(player);
    }

    public Player getPlayerById(Long id) {
        checkId(id);
        return playerRepository.findById(id).orElseThrow(() ->
            new NotFoundException("Error 404! Player not found!"));
    }

    public Player updatePlayer(Long id, Player player) {
        Player newPlayer = getPlayerById(id);

        if(player.getName() != null) {
            checkName(player.getName());
            newPlayer.setName(player.getName());
        }

        if(player.getTitle() != null) {
            checkTitle(player.getTitle());
            newPlayer.setTitle(player.getTitle());
        }

        if(player.getRace() != null) {
            checkRace(player.getRace());
            newPlayer.setRace(player.getRace());
        }

        if(player.getProfession() != null) {
            checkProfession(player.getProfession());
            newPlayer.setProfession(player.getProfession());
        }

        if(player.getBirthday() != null) {
            checkBirthday(player.getBirthday());
            newPlayer.setBirthday(player.getBirthday());
        }

        if(player.getBanned() != null) {
            newPlayer.setBanned(player.getBanned());
        }

        if(player.getExperience() != null) {
            checkExperience(player.getExperience());
            newPlayer.setExperience(player.getExperience());
        }

        newPlayer.setLevel(calculateCurrentLevel(newPlayer.getExperience()));
        newPlayer.setUntilNextLevel(calculateExperienceUntilNextLevel(newPlayer.getExperience(), newPlayer.getLevel()));

        return playerRepository.save(newPlayer);
    }

    public Player deletePlayer(Long id) {
        Player player = getPlayerById(id);
        playerRepository.delete(player);
        return player;
    }

    public void checkId(Long id) {
        if(id<=0) throw new BadRequestException("ID is invalid");
    }

    public void checkName(String name) {
        if(name==null || name.isEmpty() || name.length()>MAX_LENGTH_NAME)
            throw new BadRequestException("Name is invalid");
    }

    public void checkTitle(String title) {
        if(title.length()>MAX_LENGTH_TITLE || title==null || title.isEmpty())
            throw new BadRequestException("Title is invalid");
    }

    public void checkRace(Race race) {
        if(race==null)
            throw new BadRequestException("Race is invalid");
    }

    public void checkProfession(Profession profession) {
        if(profession==null)
            throw new BadRequestException("Profession is invalid");
    }

    public void checkBirthday(Date birthday) {
        if(birthday==null)
            throw new BadRequestException("Birthday is invalid");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(birthday.getTime());
        if (calendar.get(Calendar.YEAR) < MIN_BIRTHDAY || calendar.get(Calendar.YEAR) > MAX_BIRTHDAY)
            throw new BadRequestException("Birthday is out of bounds");
    }

    public void checkExperience(Integer experience) {
        if(experience<0 || experience>MAX_SIZE_EXPERIENCE || experience==null)
            throw new BadRequestException("Experience is invalid");
    }

    public Specification<Player> filterByName(String name) {
        return (root,query,cb)->name==null?null:cb.like(root.get("name"),"%"+name+"%");
    }

    public Specification<Player> filterByTitle(String title) {
        return (root,query,cb)->title==null?null:cb.like(root.get("title"),"%"+title+"%");
    }

    public Specification<Player> filterByRace(Race race) {
        return (root,query,cb)->race==null?null:cb.equal(root.get("race"),race);
    }

    public Specification<Player> filterByProfession(Profession profession) {
        return (root,query,cb)->profession==null?null:cb.equal(root.get("profession"),profession);
    }

    public Specification<Player> filterByExperience(Integer min,Integer max) {
        return (root,query,cb)->{
            if (min==null && max==null) return null;
            if (min==null) return cb.lessThanOrEqualTo(root.get("experience"), max);
            if (max==null) return cb.greaterThanOrEqualTo(root.get("experience"), min);
            return cb.between(root.get("experience"), min, max);
        };
    }

    public Specification<Player> filterByLevel(Integer min,Integer max) {
        return (root,query,cb)->{
            if (min==null && max==null) return null;
            if (min==null) return cb.lessThanOrEqualTo(root.get("level"), max);
            if (max==null) return cb.greaterThanOrEqualTo(root.get("level"), min);
            return cb.between(root.get("level"), min, max);
        };
    }

//    public Specification<Player> filterByUntilNextLevel(Integer min, Integer max) {
//        return (root,query,cb)->{
//            if (min==null && max==null) return null;
//            if (min==null) return cb.lessThanOrEqualTo(root.get("untilNextLevel"), max);
//            if (max==null) return cb.greaterThanOrEqualTo(root.get("untilNextLevel"), min);
//            return cb.between(root.get("untilNextLevel"), min, max);
//        };
//    }

    public Specification<Player> filterByBirthday(Long after, Long before) {
        return (root,query,cb)->{
            if (after==null && before==null) return null;
            if (after==null) return cb.lessThanOrEqualTo(root.get("birthday"), new Date(before));
            if (before==null) return cb.greaterThanOrEqualTo(root.get("birthday"), new Date(after));
            return cb.between(root.get("birthday"), new Date(after), new Date(before));
        };
    }

    public Specification<Player> filterByBanned(Boolean isBanned) {
        return (root,query,cb)->{
            if (isBanned==null) return null;
            if (isBanned) return cb.isTrue(root.get("banned"));
            return cb.isFalse(root.get("banned"));
        };
    }
}
