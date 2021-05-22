package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayerService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/players")
public class PlayerController {
    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    @ResponseBody
    public List<Player> getAll(@RequestParam(value = "name", required = false)  String name,
                               @RequestParam(value = "title", required = false)  String title,
                               @RequestParam(value = "race", required = false) Race race,
                               @RequestParam(value = "profession", required = false) Profession profession,
                               @RequestParam(value = "after", required = false) Long after,
                               @RequestParam(value = "before", required = false) Long before,
                               @RequestParam(value = "banned", required = false) Boolean banned,
                               @RequestParam(value = "minExperience", required = false) Integer minExperience,
                               @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
                               @RequestParam(value = "minLevel", required = false) Integer minLevel,
                               @RequestParam(value = "maxLevel", required = false) Integer maxLevel,
                               @RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber,
                               @RequestParam(value = "pageSize", defaultValue = "3") Integer pageSize,
                               @RequestParam(value = "order", required = false, defaultValue = "ID") PlayerOrder playerOrder) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(playerOrder.getFieldName()));

        return playerService.findAllPlayers(
                Specification.where(playerService.filterByName(name))
                        .and(playerService.filterByTitle(title))
                        .and(playerService.filterByRace(race))
                        .and(playerService.filterByProfession(profession))
                        .and(playerService.filterByExperience(minExperience, maxExperience))
                        .and(playerService.filterByLevel(minLevel, maxLevel))
                        .and(playerService.filterByBirthday(after, before))
                        .and(playerService.filterByBanned(banned)),
                        pageable).getContent();
        }

    @GetMapping("/count")
    @ResponseBody
    public Long getCount(@RequestParam(value = "name", required = false)  String name,
                         @RequestParam(value = "title", required = false)  String title,
                         @RequestParam(value = "race", required = false) Race race,
                         @RequestParam(value = "profession", required = false) Profession profession,
                         @RequestParam(value = "after", required = false) Long after,
                         @RequestParam(value = "before", required = false) Long before,
                         @RequestParam(value = "banned", required = false) Boolean banned,
                         @RequestParam(value = "minExperience", required = false) Integer minExperience,
                         @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
                         @RequestParam(value = "minLevel", required = false) Integer minLevel,
                         @RequestParam(value = "maxLevel", required = false) Integer maxLevel) {
        return playerService.getCountPlayers(Specification.where(playerService.filterByName(name))
                        .and(playerService.filterByTitle(title))
                        .and(playerService.filterByRace(race))
                        .and(playerService.filterByProfession(profession))
                        .and(playerService.filterByExperience(minExperience, maxExperience))
                        .and(playerService.filterByLevel(minLevel, maxLevel))
                        .and(playerService.filterByBirthday(after, before))
                        .and(playerService.filterByBanned(banned))
                        );
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        return ResponseEntity.ok(playerService.createPlayer(player));
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Player> getPlayerById(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.getPlayerById(id));
    }

    @PostMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Player> updatePlayer(@PathVariable Long id, @RequestBody Player player) {
        return ResponseEntity.ok(playerService.updatePlayer(id, player));
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public void deletePlayer(@PathVariable Long id) {
        playerService.deletePlayer(id);
    }
}
