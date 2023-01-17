local json = require "json"

function decodeTable(t, f)
    t = json.decode(t)
    local function printTableHelper(obj, cnt)
        local cnt = cnt or 0
        if type(obj) == "table" then
            io.write("\n", string.rep("\t", cnt), "{\n")
            cnt = cnt + 1
            for k, v in pairs(obj) do
                if type(k) == "string" then
                    io.write(string.rep("\t", cnt), '["' .. k .. '"]', " = ")
                end
                if type(k) == "number" then
                    io.write(string.rep("\t", cnt), "[" .. k .. "]", " = ")
                end
                printTableHelper(v, cnt)
                io.write(",\n")
            end
            cnt = cnt - 1
            io.write(string.rep("\t", cnt), "}")
        elseif type(obj) == "string" then
            io.write(string.format("%q", obj))
        else
            io.write(tostring(obj))
        end
    end
    if f == nil then
        printTableHelper(t)
    else
        io.output(f)
        io.write("table = ")
        printTableHelper(t)
        io.output(io.stdout)
    end
end

function encodeTable(t)
    return json.encode(t)
end

function printAll(t, tabs)
    local nesting = ""
    for i = 0, tabs, 1 do
        nesting = nesting .. "\t"
    end
    for k, v in pairs(t) do
        if type(v) == "table" then
            print(nesting .. k .. " = {")
            printAll(v, tabs + 1)
            print(nesting .. "}")
        else
            print(nesting .. k .. " = " .. v)
        end
    end
end

function tablelength(T)
    local count = 0
    for _ in pairs(T) do count = count + 1 end
    return count
end

--print("table = {")
--printAll(table, 0)
--print("}")

--print(tablelength(table))
