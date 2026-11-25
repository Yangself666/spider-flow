var grammers = [];
$.ajax({
	url : 'spider/grammers',
	type : 'post',
	dataType : 'json',
	success : function(response){
		if(response.code == 1 && Array.isArray(response.data)){
			grammers = response.data;
		}
	}
});

function getHintContext(cm){
	var cursor = cm.getCursor();
	var text = cm.getRange(CodeMirror.Pos(cm.firstLine(),0),cursor);
	var start = -1;
	var depth = 0;
	var quote = '';
	// 只在模板表达式内联想，跳过字符串中的点号、引号和括号
	for(var i = 0; i < text.length; i++){
		var ch = text.charAt(i);
		if(quote){
			if(ch === '\\'){
				i++;
			}else if(ch === quote){
				quote = '';
			}
		}else if(ch === '$' && text.charAt(i + 1) === '{'){
			start = ++i + 1;
			depth++;
		}else if(depth){
			if(ch === '"' || ch === "'"){
				quote = ch;
			}else if(ch === '{'){
				depth++;
			}else if(ch === '}'){
				depth--;
			}
		}
	}
	if(!depth || quote){
		return null;
	}
	var expression = text.substring(start);
	var prefix = (expression.match(/\b[A-Za-z_]\w*$/) || [''])[0];
	var before = expression.slice(0,expression.length - prefix.length);
	if(!prefix && /\w$/.test(before)){
		return null;
	}
	var chain = before.match(/([A-Za-z_]\w*(?:\s*\.\s*[A-Za-z_]\w*)*)\s*\.\s*$/);
	var suffix = cm.getLine(cursor.line).substring(cursor.ch);
	var rest = suffix.match(/^\w*/)[0];
	return {
		prefix : prefix,
		receiver : chain ? chain[1].split(/\s*\.\s*/) : (/\.\s*$/.test(before) ? [] : null),
		from : CodeMirror.Pos(cursor.line,cursor.ch - prefix.length),
		to : CodeMirror.Pos(cursor.line,cursor.ch + rest.length),
		opening : suffix.substring(rest.length).match(/^\s*\(/)
	};
}

function isHintProperty(entry){
	return entry['function'] && entry.owner !== entry['function'] && /^get[A-Z]/.test(entry.method) &&
		(Array.isArray(entry.parameters) ? !entry.parameters.length : Boolean(entry.example && !entry.example.includes('(')));
}

function getHintName(entry){
	return isHintProperty(entry) ? entry.method.charAt(3).toLowerCase() + entry.method.substring(4) : entry.method;
}

function getReceiverHints(receiver){
	if(!receiver.length){
		return null;
	}
	var root = receiver[0];
	var roots = grammers.filter(function(entry){
		return entry['function'] === root;
	});
	var owners = roots.filter(function(entry){
		return !entry.method;
	}).reduce(function(types,entry){
		return types.concat(entry.owner || [],entry.returns || []);
	},[]).map(function(type){return type.split('<')[0];});
	var members = grammers.filter(function(entry){
		return entry.method && (entry['function'] === root || (!entry['function'] && owners.includes(entry.owner)));
	});
	for(var i = 1; i < receiver.length && members.length; i++){
		var property = receiver[i];
		var types = members.reduce(function(result,entry){
			return isHintProperty(entry) && getHintName(entry) === property ? result.concat(entry.returns || []) : result;
		},[]);
		members = grammers.filter(function(entry){
			return entry.method && !entry['function'] && types.includes(entry.owner);
		});
	}
	return roots.length ? members : null;
}

function renderSpiderflowHint(item,data,hint){
	item.classList.add('spiderflow-hint');
	var signature = document.createElement('span');
	signature.className = 'spiderflow-hint-signature';
	signature.textContent = hint.displayText;
	item.appendChild(signature);
	var detail = [hint.description,hint.example].filter(Boolean).join('\n');
	if(detail){
		var description = document.createElement('span');
		description.className = 'spiderflow-hint-detail';
		description.textContent = detail;
		item.appendChild(description);
	}
}

function spiderflowHints(cm,options){
	var context = getHintContext(cm);
	if(!context){
		return null;
	}
	var entries = context.receiver ? getReceiverHints(context.receiver) : grammers.filter(function(entry){
		return entry['function'] && !entry.method;
	});
	if(entries === null){
		if(!context.prefix && !options.showAllMethods){
			return null;
		}
		entries = grammers.filter(function(entry){return entry.method && !entry['function'];});
	}
	var prefix = context.prefix.toLowerCase();
	var seen = new Set();
	var list = [];
	entries.forEach(function(entry){
		var name = entry.method ? getHintName(entry) : entry['function'];
		var rank = name.toLowerCase().indexOf(prefix);
		if(rank === -1){
			return;
		}
		var method = Boolean(entry.method && !isHintProperty(entry));
		var parameters = entry.parameters;
		var args = method ? '(' + (parameters ? parameters.join(', ') : '…') + ')' : '';
		var signature = name + args;
		var key = signature + '|' + (entry.owner || '');
		if(seen.has(key)){
			return;
		}
		seen.add(key);
		list.push({
			name : name,
			rank : rank,
			arity : parameters ? parameters.length : 0,
			text : name + (method ? '()' : ''),
			displayText : signature + (entry.returns && entry.returns.length ? ' → ' + entry.returns.join(' / ') : ''),
			description : [entry.owner !== entry['function'] && entry.owner,entry.comment].filter(Boolean).join(' · '),
			example : entry.example,
			render : renderSpiderflowHint,
			hint : function(editor,data,hint){
				var existingCall = method && context.opening;
				editor.replaceRange(existingCall ? name : hint.text,data.from,data.to,'complete');
				if(method && (existingCall || !parameters || parameters.length)){
					var offset = existingCall ? context.opening[0].length : 1;
					editor.setCursor(CodeMirror.Pos(data.from.line,data.from.ch + name.length + offset));
				}
			}
		});
	});
	list.sort(function(a,b){
		return a.rank - b.rank || a.name.localeCompare(b.name) || a.arity - b.arity || a.displayText.localeCompare(b.displayText);
	});
	return list.length ? {list:list,from:context.from,to:context.to} : null;
}

function initHint(cm){
	function show(editor,showAllMethods){
		editor.showHint({hint:spiderflowHints,completeSingle:false,showAllMethods:showAllMethods});
	}
	cm.addKeyMap({
		'Ctrl-Space' : function(editor){show(editor,true);},
		'Cmd-Space' : function(editor){show(editor,true);}
	});
	cm.on('inputRead',function(editor,change){
		var ending = change.text[change.text.length - 1];
		if(/[.{]$/.test(ending) || (!editor.state.completionActive && /\w$/.test(ending))){
			show(editor,false);
		}
	});
}
