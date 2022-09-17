package com.game.service;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.exceptions.BadRequestException;
import com.game.exceptions.NotFoundException;
import com.game.repository.PlayerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

@Service
public class PlayerService {
    private  static final int MAX_NAME = 12;
    private static final int MAX_TITLE = 30;
    private static  final int MAX_EXPERIENCE = 10000000;
    private static final long MIN_BIRTHDAY = 2000;
    private static final long MAX_BIRTHDAY = 3000;
    private PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public Integer getCurrentLvl(Integer exp) {
        return (((int)Math.sqrt(2500+200*exp))-50)/100;
    }

    public Integer getUntilNextLvl(Integer lvl, Integer exp) {
        return 50*(lvl+1)*(lvl+2)-exp;
    }

    public Page<Player> findAllPlayers(Specification<Player> spec, Pageable page) {
        return playerRepository.findAll(spec, page);
    }

    public Long countAllPlayers(Specification<Player> spec) {
        return playerRepository.count(spec);
    }

    public void checkId(Long id) {
        if(id<=0) throw new BadRequestException("ошибка id");
    }

    public void checkName(String name) {
        if (name == null || name.isEmpty() || name.length() > MAX_NAME)
            throw new BadRequestException("неподхожящее имя");
    }

    public void checkTitle(String title) {
        if (title == null || title.isEmpty() || title.length() > MAX_TITLE)
            throw new BadRequestException("ошибка тайтла");
    }

    public void checkRace(Race race) {
        if (race == null) throw new BadRequestException("ошибка расы");
    }

    public void checkProfession(Profession prof) {
        if (prof == null) throw new BadRequestException("ошибка профессии");
    }

    public void checkBirthday(Date birthday) {
        if (birthday == null) throw new BadRequestException();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(birthday.getTime());
        if (calendar.get(Calendar.YEAR) < MIN_BIRTHDAY || calendar.get(Calendar.YEAR) > MAX_BIRTHDAY)
            throw new BadRequestException("ошибка даты");
    }

    public void checkExp(Integer exp) {
        if (exp == null || exp < 0 || exp > MAX_EXPERIENCE)
            throw new BadRequestException("ошибка опыта");
    }

    public Player createNewPlayer(Player player) {
        checkName(player.getName());
        checkBirthday(player.getBirthday());
        checkTitle(player.getTitle());
        checkRace(player.getRace());
        checkProfession(player.getProfession());
        checkExp(player.getExperience());
        if (player.getBanned() == null) player.setBanned(false);
        player.setLevel(getCurrentLvl(player.getExperience()));
        player.setUntilNextLevel(getUntilNextLvl(player.getLevel(), player.getExperience()));
        return playerRepository.saveAndFlush(player);
    }

    public Player getPlayerWithId(Long id) {
        checkId(id);
        return playerRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Error 404! Player not found!"));
    }

    public Player updatePlayer(Long id, Player player) {
        Player newPlayer = getPlayerWithId(id);

        if (player.getName() != null) {
            checkName(player.getName());
            newPlayer.setName(player.getName());
        }

        if (player.getTitle() != null) {
            checkTitle(player.getTitle());
            newPlayer.setTitle(player.getTitle());
        }

        if (player.getRace() != null) {
            checkRace(player.getRace());
            newPlayer.setRace(player.getRace());
        }

        if (player.getProfession() != null) {
            checkProfession(player.getProfession());
            newPlayer.setProfession(player.getProfession());
        }

        if (player.getBirthday() != null) {
            checkBirthday(player.getBirthday());
            newPlayer.setBirthday(player.getBirthday());
        }

        if (player.getBanned() != null) {
            newPlayer.setBanned(player.getBanned());
        }

        if (player.getExperience() != null) {
            checkExp(player.getExperience());
            newPlayer.setExperience(player.getExperience());
        }

        newPlayer.setLevel(getCurrentLvl(newPlayer.getExperience()));
        newPlayer.setUntilNextLevel(getUntilNextLvl(newPlayer.getLevel(), newPlayer.getExperience()));

        return playerRepository.save(newPlayer);
    }

    public Player deletePlayer(Long id) {
        Player player = getPlayerWithId(id);
        playerRepository.delete(player);
        return player;
    }




    public Specification<Player> filterByName(String name) {
        return (root,query,cb) -> name == null  ?  null  :  cb.like(root.get("name"),"%"+name+"%");
    }

    public Specification<Player> filterByTitle(String title) {
        return (root,query,cb) -> title == null  ?  null  :  cb.like(root.get("title"),"%"+title+"%");
    }

    public Specification<Player> filterByRace(Race race) {
        return (root,query,cb) -> race == null  ?  null  :  cb.equal(root.get("race"),race);
    }

    public Specification<Player> filterByProfession(Profession profession) {
        return (root,query,cb) -> profession == null  ?  null  :  cb.equal(root.get("profession"),profession);
    }

    public Specification<Player> filterByExperience(Integer min, Integer max) {
        return (root,query,cb) -> {
            if (min == null && max == null) return null;
            if (min == null) return cb.lessThanOrEqualTo(root.get("experience"), max);
            if (max==null) return cb.greaterThanOrEqualTo(root.get("experience"), min);
            return cb.between(root.get("experience"), min, max);
        };
    }

    public Specification<Player> filterByLevel(Integer min, Integer max) {
        return (root,query,cb) -> {
            if (min == null && max == null) return null;
            if (min == null) return cb.lessThanOrEqualTo(root.get("level"), max);
            if (max == null) return cb.greaterThanOrEqualTo(root.get("level"), min);
            return cb.between(root.get("level"), min, max);
        };
    }

    public Specification<Player> filterByUntilNextLevel(Integer min, Integer max) {
       return (root,query,cb) -> {
            if (min == null && max == null) return null;
            if (min == null) return cb.lessThanOrEqualTo(root.get("untilNextLevel"), max);
            if (max == null) return cb.greaterThanOrEqualTo(root.get("untilNextLevel"), min);
            return cb.between(root.get("untilNextLevel"), min, max);
        };
    }

    public Specification<Player> filterByBirthday(Long after, Long before) {
        return (root,query,cb) -> {
            if (after == null && before == null) return null;
            if (after == null) return cb.lessThanOrEqualTo(root.get("birthday"), new Date(before));
            if (before == null) return cb.greaterThanOrEqualTo(root.get("birthday"), new Date(after));
            return cb.between(root.get("birthday"), new Date(after), new Date(before));
        };
    }

    public Specification<Player> filterByBanned(Boolean isBanned) {
        return (root,query,cb) -> {
            if (isBanned == null) return null;
            if (isBanned) return cb.isTrue(root.get("banned"));
            return cb.isFalse(root.get("banned"));
        };
    }

}
