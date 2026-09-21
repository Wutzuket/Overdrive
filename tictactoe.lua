-- Vertikales Tic-Tac-Toe für den Advanced Hologram Block (inkl. Live-Text & Positionierung)

-- ===== POSITIONIERUNGS-VARIABLEN (Hier anpassen) =====
local offsetX = 0.0
local offsetY = 0.0
local offsetZ = 2.0
-- ====================================================

local board = {
    0, 0, 0,
    0, 0, 0,
    0, 0, 0
}

local currentPlayer = 1
local gameOver = false

-- Originale Positionen der Buttons speichern
local buttonOrigPos = {}

local buttons = {}
local index = 1

for row = 0, 2 do
    for col = 0, 2 do
        local posX = ((col - 1) * 0.7) + offsetX
        local posY = (((1 - row) * 0.7) + 2.5) + offsetY
        local posZ = offsetZ

        buttons[index] = button(posX, posY, posZ, 0.6, 0.6, 0.2, 0.4, 0.4, 0.4)
        buttonOrigPos[index] = {x = posX, y = posY, z = posZ}
        index = index + 1
    end
end

local statusText = text(offsetX, 3.8 + offsetY, offsetZ, 0.015, 0, 0, 0, "Vertikales Tic-Tac-Toe: Spieler X ist dran", {1, 1, 1}, "center")

-- Play Again Elemente (initial versteckt via Z = -100)
local hideZ = -100
local centerX = offsetX
local centerY = 2.8 + offsetY
local centerZ = offsetZ + 0.3

local resultTxt = text(centerX, centerY + 1.0, centerZ, 0.05, 0, 0, 0, "", {1, 1, 1}, "center")
local playAgainBtn = button(centerX, centerY, centerZ, 2.2, 0.8, 0.2, 0.15, 0.55, 0.15)
local playAgainTxt = text(centerX, centerY, centerZ + 0.15, 0.025, 0, 0, 0, "PLAY AGAIN", {1, 1, 1}, "center")

resultTxt:transition(centerX, centerY + 1.0, hideZ, 0.05, 0, 0, 0, "", {1, 1, 1}, 2)
playAgainBtn:transition(centerX, centerY, hideZ, 2.2, 0.8, 0.2, 0, 0, 0, {0.15, 0.55, 0.15}, 2)
playAgainTxt:transition(centerX, centerY, hideZ, 0.025, 0, 0, 0, "PLAY AGAIN", {1, 1, 1}, 2)

local function hideBoard()
    for i = 1, 9 do
        local p = buttonOrigPos[i]
        buttons[i]:transition(p.x, p.y, hideZ, 0.6, 0.6, 0.2, 0, 0, 0, {0.4, 0.4, 0.4}, 2)
    end
    statusText:transition(statusText.x, statusText.y, hideZ, 0.015, 0, 0, 0, "", {1, 1, 1}, 2)
end

local function showBoard()
    for i = 1, 9 do
        local p = buttonOrigPos[i]
        buttons[i]:transition(p.x, p.y, p.z, 0.6, 0.6, 0.2, 0, 0, 0, {0.4, 0.4, 0.4}, 2)
    end
    statusText:transition(statusText.x, 3.8 + offsetY, offsetZ, 0.015, 0, 0, 0, "Spieler X ist dran", {1, 1, 1}, 2)
end

local function showPlayAgain(resultMsg, resultColor)
    hideBoard()
    resultTxt:transition(centerX, centerY + 1.0, centerZ, 0.05, 0, 0, 0, resultMsg, resultColor, 2)
    playAgainBtn:transition(centerX, centerY, centerZ, 2.2, 0.8, 0.2, 0, 0, 0, {0.15, 0.55, 0.15}, 2)
    playAgainTxt:transition(centerX, centerY, centerZ + 0.15, 0.025, 0, 0, 0, "PLAY AGAIN", {1, 1, 1}, 2)
end

local function hidePlayAgain()
    resultTxt:transition(resultTxt.x, resultTxt.y, hideZ, 0.05, 0, 0, 0, "", {1, 1, 1}, 2)
    playAgainBtn:transition(playAgainBtn.x, playAgainBtn.y, hideZ, 2.2, 0.8, 0.2, 0, 0, 0, {0.15, 0.55, 0.15}, 2)
    playAgainTxt:transition(playAgainTxt.x, playAgainTxt.y, hideZ, 0.025, 0, 0, 0, "PLAY AGAIN", {1, 1, 1}, 2)
end

local function resetGame()
    for i = 1, 9 do
        board[i] = 0
    end
    currentPlayer = 1
    gameOver = false
    hidePlayAgain()
    showBoard()
end

local function checkWin()
    local winConditions = {
        {1, 2, 3}, {4, 5, 6}, {7, 8, 9},
        {1, 4, 7}, {2, 5, 8}, {3, 6, 9},
        {1, 5, 9}, {3, 5, 7}
    }

    for _, cond in ipairs(winConditions) do
        local a, b, c = cond[1], cond[2], cond[3]
        if board[a] ~= 0 and board[a] == board[b] and board[a] == board[c] then
            return board[a]
        end
    end

    local draw = true
    for i = 1, 9 do
        if board[i] == 0 then
            draw = false
            break
        end
    end

    if draw then return 3 end
    return 0
end

print("Vertikales Tic-Tac-Toe gestartet! Spieler X beginnt.")

while true do
    if gameOver then
        if playAgainBtn:isClicked() then
            resetGame()
            print("Neues Spiel gestartet!")
        end
    else
        for i = 1, 9 do
            if buttons[i]:isClicked() and board[i] == 0 then
                board[i] = currentPlayer

                if currentPlayer == 1 then
                    buttons[i]:transition(buttons[i].x, buttons[i].y, buttons[i].z,
                                          0.6, 0.6, 0.2, 0, 0, 0,
                                          {1.0, 0.2, 0.2}, 2)
                    print("Spieler X hat Feld " .. i .. " gewählt.")
                    currentPlayer = 2
                    statusText:transition(statusText.x, statusText.y, statusText.z,
                                          0.015, 0, 0, 0,
                                          "Spieler O ist dran",
                                          {0.2, 0.2, 1.0}, 2)
                else
                    buttons[i]:transition(buttons[i].x, buttons[i].y, buttons[i].z,
                                          0.6, 0.6, 0.2, 0, 0, 0,
                                          {0.2, 0.2, 1.0}, 2)
                    print("Spieler O hat Feld " .. i .. " gewählt.")
                    currentPlayer = 1
                    statusText:transition(statusText.x, statusText.y, statusText.z,
                                          0.015, 0, 0, 0,
                                          "Spieler X ist dran",
                                          {1.0, 0.2, 0.2}, 2)
                end

                local result = checkWin()
                if result == 1 then
                    print("--- SPIELER X GEWINNT! ---")
                    showPlayAgain("SPIELER X GEWINNT!", {1.0, 0.2, 0.2})
                    gameOver = true
                elseif result == 2 then
                    print("--- SPIELER O GEWINNT! ---")
                    showPlayAgain("SPIELER O GEWINNT!", {0.2, 0.2, 1.0})
                    gameOver = true
                elseif result == 3 then
                    print("--- UNENTSCHIEDEN! ---")
                    showPlayAgain("UNENTSCHIEDEN!", {1, 1, 0})
                    gameOver = true
                end
            end
        end
    end

    sleep(1)
end
