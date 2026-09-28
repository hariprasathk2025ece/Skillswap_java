// =============================================
// SkillSwap - Simple Table Frontend Logic
// =============================================

// Helper: Make API calls
async function callApi(url, method = 'GET', body = null) {
    const options = {
        method: method,
        headers: { 'Content-Type': 'application/json' }
    };
    if (body) options.body = JSON.stringify(body);

    try {
        const response = await fetch(url, options);
        if (response.status === 204) {
            return { ok: true, status: 204, data: null };
        }
        const text = await response.text();
        let data;
        try {
            data = JSON.parse(text);
        } catch (e) {
            data = text;
        }
        return { ok: response.ok, status: response.status, data };
    } catch (err) {
        return { ok: false, status: 0, data: 'Connection error: ' + err.message };
    }
}

// Helper: Render single object as a 2-column table (Field | Value)
function renderObjectTable(obj, successMsg = '') {
    if (!obj || typeof obj !== 'object') {
        return `<p>${obj}</p>`;
    }

    let rows = '';
    for (const [key, value] of Object.entries(obj)) {
        let displayValue = value;
        if (typeof value === 'object' && value !== null) {
            displayValue = Object.entries(value)
                .map(([k, v]) => `${k}: ${v}`)
                .join(' | ');
        }
        rows += `<tr><td class="key-col">${formatHeader(key)}</td><td>${displayValue !== null && displayValue !== undefined ? displayValue : '-'}</td></tr>`;
    }

    let html = '';
    if (successMsg) {
        html += `<span class="success-tag">✓ ${successMsg}</span>`;
    }
    html += `<table><thead><tr><th>Field</th><th>Value</th></tr></thead><tbody>${rows}</tbody></table>`;
    return html;
}

// Helper: Render array of objects as a multi-column table
function renderArrayTable(arr, type = '') {
    if (!arr || !Array.isArray(arr) || arr.length === 0) {
        return '<p class="empty-msg">No records found.</p>';
    }

    const keys = Object.keys(arr[0]);
    let headers = keys.map(k => `<th>${formatHeader(k)}</th>`).join('');
    if (type === 'member' || type === 'skill') {
        headers += '<th>Action</th>';
    }

    let rows = arr.map(obj => {
        let cells = keys.map(k => {
            let val = obj[k];
            if (typeof val === 'object' && val !== null) {
                if (val.name) {
                    val = `${val.name} (ID: ${val.id})`;
                } else {
                    val = Object.entries(val).map(([ik, iv]) => `${ik}: ${iv}`).join(', ');
                }
            }
            return `<td>${val !== null && val !== undefined ? val : '-'}</td>`;
        }).join('');

        if (type === 'member') {
            cells += `<td><button class="btn-table-delete" onclick="quickDeleteMember(${obj.id})">Delete</button></td>`;
        } else if (type === 'skill') {
            cells += `<td><button class="btn-table-delete" onclick="quickDeleteSkill(${obj.id})">Delete</button></td>`;
        }

        return `<tr>${cells}</tr>`;
    }).join('');

    return `<table><thead><tr>${headers}</tr></thead><tbody>${rows}</tbody></table>`;
}

// Helper: Format field names (e.g. creditBalance -> Credit Balance)
function formatHeader(key) {
    return key
        .replace(/([A-Z])/g, ' $1')
        .replace(/^./, str => str.toUpperCase())
        .trim();
}

// Helper: Display in Result div
function showResult(elementId, htmlContent, isError = false) {
    const el = document.getElementById(elementId);
    if (!el) return;

    el.classList.add('show');
    if (isError) {
        el.classList.add('error');
        const msg = typeof htmlContent === 'object' ? (htmlContent.message || JSON.stringify(htmlContent)) : htmlContent;
        el.innerHTML = `<p>❌ Error: ${msg}</p>`;
    } else {
        el.classList.remove('error');
        el.innerHTML = htmlContent;
    }
}

// =============================================
// 1. MEMBER FUNCTIONS
// =============================================

async function addMember() {
    const name = document.getElementById('memberName').value.trim();
    const email = document.getElementById('memberEmail').value.trim();
    const credits = parseInt(document.getElementById('memberCredits').value) || 0;

    if (!name || !email) {
        showResult('memberResult', 'Please provide both name and email', true);
        return;
    }

    const res = await callApi('/api/members', 'POST', { name, email, creditBalance: credits });
    if (res.ok) {
        showResult('memberResult', renderObjectTable(res.data, 'Member Added Successfully'));
        document.getElementById('memberName').value = '';
        document.getElementById('memberEmail').value = '';
    } else {
        showResult('memberResult', res.data, true);
    }
}

async function getAllMembers() {
    const res = await callApi('/api/members');
    if (res.ok) {
        showResult('allMembersResult', renderArrayTable(res.data, 'member'));
    } else {
        showResult('allMembersResult', res.data, true);
    }
}

async function deleteMemberById() {
    const memberId = parseInt(document.getElementById('deleteMemberId').value);
    if (!memberId) {
        showResult('deleteMemberResult', 'Please enter a valid Member ID', true);
        return;
    }

    const res = await callApi(`/api/members/${memberId}`, 'DELETE');
    if (res.ok) {
        const table = `
            <span class="success-tag">✓ Member #${memberId} Deleted Successfully</span>
            <table>
                <thead><tr><th>Field</th><th>Value</th></tr></thead>
                <tbody>
                    <tr><td class="key-col">Deleted Member ID</td><td>#${memberId}</td></tr>
                    <tr><td class="key-col">Status</td><td>Removed from Database</td></tr>
                </tbody>
            </table>
        `;
        showResult('deleteMemberResult', table);
        document.getElementById('deleteMemberId').value = '';
        getAllMembers();
    } else {
        showResult('deleteMemberResult', res.data, true);
    }
}

async function quickDeleteMember(id) {
    if (!confirm(`Are you sure you want to delete Member #${id}?`)) return;
    const res = await callApi(`/api/members/${id}`, 'DELETE');
    if (res.ok) {
        getAllMembers();
    } else {
        alert('Failed to delete member: ' + (res.data.message || res.data));
    }
}

// =============================================
// 2. SKILL FUNCTIONS
// =============================================

async function addSkill() {
    const skillName = document.getElementById('skillName').value.trim();
    const description = document.getElementById('skillDescription').value.trim();
    const hoursAvailable = parseInt(document.getElementById('skillHours').value);
    const providerId = parseInt(document.getElementById('skillProviderId').value);

    if (!skillName || !providerId || !hoursAvailable) {
        showResult('skillResult', 'Please fill in Skill Name, Provider ID, and Hours Available', true);
        return;
    }

    const res = await callApi('/api/skills', 'POST', { skillName, description, hoursAvailable, providerId });
    if (res.ok) {
        showResult('skillResult', renderObjectTable(res.data, 'Skill Offer Added Successfully'));
        document.getElementById('skillName').value = '';
        document.getElementById('skillDescription').value = '';
    } else {
        showResult('skillResult', res.data, true);
    }
}

async function getAllSkills() {
    const res = await callApi('/api/skills');
    if (res.ok) {
        showResult('allSkillsResult', renderArrayTable(res.data, 'skill'));
    } else {
        showResult('allSkillsResult', res.data, true);
    }
}

async function deleteSkillById() {
    const skillId = parseInt(document.getElementById('deleteSkillId').value);
    if (!skillId) {
        showResult('deleteSkillResult', 'Please enter a valid Skill ID', true);
        return;
    }

    const res = await callApi(`/api/skills/${skillId}`, 'DELETE');
    if (res.ok) {
        const table = `
            <span class="success-tag">✓ Skill Offer #${skillId} Deleted Successfully</span>
            <table>
                <thead><tr><th>Field</th><th>Value</th></tr></thead>
                <tbody>
                    <tr><td class="key-col">Deleted Skill ID</td><td>#${skillId}</td></tr>
                    <tr><td class="key-col">Status</td><td>Removed from Database</td></tr>
                </tbody>
            </table>
        `;
        showResult('deleteSkillResult', table);
        document.getElementById('deleteSkillId').value = '';
        getAllSkills();
    } else {
        showResult('deleteSkillResult', res.data, true);
    }
}

async function quickDeleteSkill(id) {
    if (!confirm(`Are you sure you want to delete Skill #${id}?`)) return;
    const res = await callApi(`/api/skills/${id}`, 'DELETE');
    if (res.ok) {
        getAllSkills();
    } else {
        alert('Failed to delete skill: ' + (res.data.message || res.data));
    }
}

// =============================================
// 3. SESSION FUNCTIONS
// =============================================

async function requestSkill() {
    const skillOfferId = parseInt(document.getElementById('reqSkillId').value);
    const requesterId = parseInt(document.getElementById('reqRequesterId').value);
    const hoursRequested = parseInt(document.getElementById('reqHours').value);

    if (!skillOfferId || !requesterId || !hoursRequested) {
        showResult('requestResult', 'Please fill in Skill ID, Requester ID, and Hours', true);
        return;
    }

    const res = await callApi('/api/session-requests', 'POST', { skillOfferId, requesterId, hoursRequested });
    if (res.ok) {
        showResult('requestResult', renderObjectTable(res.data, 'Session Requested (Status: PENDING)'));
    } else {
        showResult('requestResult', res.data, true);
    }
}

async function getAllSessions() {
    const res = await callApi('/api/session-requests');
    if (res.ok) {
        showResult('allSessionsResult', renderArrayTable(res.data));
    } else {
        showResult('allSessionsResult', res.data, true);
    }
}

async function confirmSession() {
    const sessionId = parseInt(document.getElementById('confirmSessionId').value);
    if (!sessionId) {
        showResult('confirmResult', 'Please enter a Session ID', true);
        return;
    }

    const res = await callApi(`/api/session-requests/${sessionId}/confirm`, 'PUT');
    if (res.ok) {
        showResult('confirmResult', renderObjectTable(res.data, `Session #${sessionId} Confirmed`));
    } else {
        showResult('confirmResult', res.data, true);
    }
}

async function completeSession() {
    const sessionId = parseInt(document.getElementById('completeSessionId').value);
    const actualHours = parseInt(document.getElementById('actualHours').value);

    if (!sessionId || !actualHours) {
        showResult('completeResult', 'Please enter Session ID and Actual Hours Delivered', true);
        return;
    }

    const res = await callApi(`/api/session-requests/${sessionId}/complete`, 'PUT', { actualHours });
    if (res.ok) {
        showResult('completeResult', renderObjectTable(res.data, `Session #${sessionId} Completed & Credits Transferred`));
    } else {
        showResult('completeResult', res.data, true);
    }
}

// =============================================
// 4. BALANCE AND LEDGER
// =============================================

async function checkBalance() {
    const memberId = parseInt(document.getElementById('balanceMemberId').value);
    if (!memberId) {
        showResult('balanceResult', 'Please enter a Member ID', true);
        return;
    }

    const res = await callApi(`/api/members/${memberId}/balance`);
    if (res.ok) {
        const table = `
            <table>
                <thead><tr><th>Field</th><th>Value</th></tr></thead>
                <tbody>
                    <tr><td class="key-col">Member ID</td><td>${memberId}</td></tr>
                    <tr><td class="key-col">Credit Balance</td><td><strong>${res.data.creditBalance !== undefined ? res.data.creditBalance : res.data}</strong> Credits</td></tr>
                </tbody>
            </table>
        `;
        showResult('balanceResult', table);
    } else {
        showResult('balanceResult', res.data, true);
    }
}

async function viewLedger() {
    const memberId = parseInt(document.getElementById('ledgerMemberId').value);
    if (!memberId) {
        showResult('ledgerResult', 'Please enter a Member ID', true);
        return;
    }

    const res = await callApi(`/api/ledger/member/${memberId}`);
    if (res.ok) {
        showResult('ledgerResult', renderArrayTable(res.data));
    } else {
        showResult('ledgerResult', res.data, true);
    }
}
